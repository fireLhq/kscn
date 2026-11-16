package top.kscn.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.kscn.entity.dto.FileOperationRequest;
import top.kscn.exception.CustomException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.util.*;
import java.util.function.UnaryOperator;

/** 文件操作先预览，再基于同一份目录快照确认，整个批次在一个事务中执行。 */
@Service
@RequiredArgsConstructor
public class FileOperationService {
    private final JdbcTemplate jdbc;
    private final RecycleBinService namespace;
    private final FileStorageService storage;
    private final ObjectMapper json;

    public record Node(Long id, Long parent, String name, boolean folder, Long storageId,
                       long size, String type, String category, String updated) {}
    public record Entry(Long fileId, String originalName, Long targetParentId, String newName,
                        boolean conflict, boolean skipped, boolean unchanged) {}
    public record Preview(List<Entry> entries, String previewToken, boolean canExecute, long copyBytes) {}

    private String table(Long userId) { return userId == null ? "public_file" : "user_file"; }
    private List<Node> snapshot(Long userId) {
        return jdbc.query("SELECT file_id, parent_id, file_name, is_folder, storage_id, file_size, file_type, "
                        + (userId == null ? "category" : "NULL AS category") + ", update_time FROM " + table(userId)
                        + " WHERE is_deleted = 0" + (userId == null ? "" : " AND user_id = " + userId)
                        + " ORDER BY file_id FOR UPDATE",
                (rs, index) -> new Node(rs.getLong("file_id"), (Long) rs.getObject("parent_id"), rs.getString("file_name"),
                        rs.getInt("is_folder") == 1, (Long) rs.getObject("storage_id"), rs.getLong("file_size"),
                        rs.getString("file_type"), rs.getString("category"), rs.getString("update_time")));
    }

    @Transactional
    public Preview preview(Long userId, FileOperationRequest request) {
        return previewBetween(userId, userId, request);
    }

    @Transactional
    public Preview previewTransfer(Long sourceUser, Long actor, FileOperationRequest request) {
        return previewBetween(sourceUser, transferTarget(sourceUser, actor, request), request);
    }

    @Transactional
    public Map<String, Object> executeTransfer(Long sourceUser, Long actor, FileOperationRequest request) {
        return executeBetween(sourceUser, transferTarget(sourceUser, actor, request), request);
    }

    private Long transferTarget(Long sourceUser, Long actor, FileOperationRequest request) {
        if (actor == null || (sourceUser != null && !sourceUser.equals(actor))) throw new CustomException(401, "请登录后操作自己的云盘");
        if (!"COPY".equals(request.getAction())) throw new CustomException(400, "跨空间仅支持复制");
        Long target;
        if ("PUBLIC".equals(request.getTargetSpace())) target = null;
        else if ("USER".equals(request.getTargetSpace())) target = actor;
        else throw new CustomException(400, "请选择目标空间");
        if (Objects.equals(sourceUser, target)) throw new CustomException(400, "请选择另一个文件空间");
        return target;
    }

    private Preview previewBetween(Long sourceUser, Long targetUser, FileOperationRequest request) {
        boolean cross = !Objects.equals(sourceUser, targetUser);
        List<Node> nodes, destinations;
        if (cross) {
            // 两个方向始终先锁公共空间，再锁个人空间，之后才读取快照，避免互相等待。
            namespace.lockForWrite(null, targetUser == null ? request.getTargetParentId() : null);
            namespace.lockForWrite(sourceUser == null ? targetUser : sourceUser, targetUser != null ? request.getTargetParentId() : null);
            List<Node> publicNodes = snapshot(null);
            List<Node> userNodes = snapshot(sourceUser == null ? targetUser : sourceUser);
            nodes = sourceUser == null ? publicNodes : userNodes;
            destinations = targetUser == null ? publicNodes : userNodes;
        } else {
            namespace.lockForWrite(sourceUser, "RENAME".equals(request.getAction()) ? null : request.getTargetParentId());
            nodes = snapshot(sourceUser); destinations = nodes;
        }
        List<Entry> entries = planBetween(request, nodes, destinations, cross, databaseNameWeights(targetUser));
        long bytes = copyBytes(request, entries, nodes);
        checkSpace(targetUser, bytes);
        return new Preview(entries, token(request, nodes, destinations, sourceUser, targetUser), entries.stream().noneMatch(Entry::conflict), bytes);
    }

    @Transactional
    public Map<String, Object> execute(Long userId, FileOperationRequest request) {
        return executeBetween(userId, userId, request);
    }

    private Map<String, Object> executeBetween(Long sourceUser, Long targetUser, FileOperationRequest request) {
        Preview preview = previewBetween(sourceUser, targetUser, request);
        if (request.getPreviewToken() == null || !request.getPreviewToken().equals(preview.previewToken()))
            throw new CustomException(409, "文件或目录已变化，请重新预览并确认");
        if (!preview.canExecute()) throw new CustomException(409, "存在重名，请修改名称或选择跳过/自动序号后重新预览");
        List<Node> nodes = snapshot(sourceUser);
        Map<Long, Node> byId = index(nodes);
        List<Entry> effective = preview.entries().stream().filter(item -> !item.skipped() && !item.unchanged()).toList();
        if ("RENAME".equals(request.getAction())) {
            // 临时名称释放整个批次的旧名称，支持名称交换；事务失败时全部回滚。
            String prefix = ".rename-" + UUID.randomUUID() + "-";
            for (Entry entry : effective)
                jdbc.update("UPDATE " + table(sourceUser) + " SET file_name = ? WHERE file_id = ?", prefix + entry.fileId(), entry.fileId());
        }
        for (Entry entry : effective) {
            Node source = byId.get(entry.fileId());
            if ("COPY".equals(request.getAction())) {
                copyTree(targetUser, source, entry.targetParentId(), entry.newName(), nodes);
            } else {
                jdbc.update("UPDATE " + table(sourceUser) + " SET parent_id = ?, file_name = ?, file_type = ?, update_time = NOW() WHERE file_id = ?",
                        entry.targetParentId(), entry.newName(), fileType(source, entry.newName()), entry.fileId());
            }
        }
        if (targetUser != null && "COPY".equals(request.getAction()))
            jdbc.update("UPDATE user SET used_space = (SELECT COALESCE(SUM(file_size), 0) FROM user_file WHERE user_id = ? AND is_folder = 0) WHERE user_id = ?", targetUser, targetUser);
        return Map.of("processed", effective.size(), "skipped", preview.entries().stream().filter(Entry::skipped).count(),
                "unchanged", preview.entries().stream().filter(Entry::unchanged).count(), "entries", preview.entries());
    }

    private String token(FileOperationRequest request, List<Node> nodes, List<Node> destinations, Long sourceUser, Long targetUser) {
        try {
            Map<String, Object> parameters = new LinkedHashMap<>();
            parameters.put("action", request.getAction()); parameters.put("ids", request.getFileIds());
            parameters.put("target", request.getTargetParentId()); parameters.put("policy", request.getConflictPolicy());
            parameters.put("base", request.getBaseName()); parameters.put("style", request.getNameStyle());
            parameters.put("digits", request.getDigits()); parameters.put("names", new TreeMap<>(request.getCustomNames()));
            parameters.put("sourceUser", sourceUser); parameters.put("targetUser", targetUser); parameters.put("targetSpace", request.getTargetSpace());
            // 下载计数引起的更新时间变化不影响操作方案，避免公共目录中下载导致无效重试。
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(
                    json.writeValueAsString(List.of(parameters, structure(nodes), structure(destinations))).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) { throw new IllegalStateException("无法生成操作预览", exception); }
    }

    private List<Node> structure(List<Node> nodes) {
        return nodes.stream().map(node -> new Node(node.id(), node.parent(), node.name(), node.folder(),
                node.storageId(), node.size(), node.type(), node.category(), null)).toList();
    }

    private Map<Long, Node> index(List<Node> nodes) {
        Map<Long, Node> result = new LinkedHashMap<>();
        nodes.forEach(node -> result.put(node.id(), node));
        return result;
    }

    private boolean ancestor(Long ancestor, Long target, Map<Long, Node> nodes) {
        Set<Long> visited = new HashSet<>();
        while (target != null) {
            if (!visited.add(target)) throw new CustomException(400, "目录结构异常");
            if (target.equals(ancestor)) return true;
            Node node = nodes.get(target);
            if (node == null || !node.folder()) throw new CustomException(404, "目录已删除或无权访问");
            target = node.parent();
        }
        return false;
    }

    private UnaryOperator<String> databaseNameWeights(Long userId) {
        String collation = jdbc.queryForObject("SELECT COLLATION_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = 'file_name'",
                String.class, table(userId));
        if (collation == null || !collation.matches("[a-zA-Z0-9_]+")) throw new IllegalStateException("无法读取文件名排序规则");
        Map<String, String> cache = new HashMap<>();
        // 已有名称一次性取权重，避免每个文件名各执行一次SQL。
        for (Map<String, Object> row : jdbc.queryForList("SELECT file_name, HEX(WEIGHT_STRING(RTRIM(file_name))) AS name_weight FROM "
                + table(userId) + " WHERE is_deleted = 0" + (userId == null ? "" : " AND user_id = " + userId)))
            cache.put((String) row.get("file_name"), (String) row.get("name_weight"));
        return name -> cache.computeIfAbsent(name, value -> jdbc.queryForObject(
                "SELECT HEX(WEIGHT_STRING(RTRIM(CONVERT(? USING utf8mb4)) COLLATE " + collation + "))", String.class, value));
    }

    private static String key(Long parent, String name, UnaryOperator<String> weights) {
        return parent + ":" + weights.apply(name);
    }

    static void validName(String name) {
        if (name == null || name.isBlank() || name.length() > 255 || !name.equals(name.strip())
                || name.equals(".") || name.equals("..") || name.matches(".*[\\\\/:*?\"<>|\\p{Cntrl}].*"))
            throw new CustomException(400, "名称不能为空、超过255字符，或包含路径分隔符及非法字符");
    }

    private String numbered(String name, boolean folder, int number) {
        int dot = folder ? -1 : name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        String ext = dot > 0 ? name.substring(dot) : "";
        String suffix = "(" + number + ")";
        if (ext.length() + suffix.length() >= 255) throw new CustomException(400, "扩展名过长，无法自动添加序号，请手动改名");
        int end = Math.min(base.length(), 255 - ext.length() - suffix.length());
        if (end > 0 && Character.isHighSurrogate(base.charAt(end - 1))) end--;
        return base.substring(0, end) + suffix + ext;
    }

    private String renamed(FileOperationRequest request, Node node, int position) {
        validName(request.getBaseName());
        // 位数为最小宽度，编号超出宽度时保留完整数字，绝不截断或循环编号。
        String number = String.format(Locale.ROOT, "%0" + request.getDigits() + "d", position);
        String serial = switch (request.getNameStyle()) {
            case "PARENTHESES" -> "(" + number + ")";
            case "DASH" -> "-" + number;
            case "UNDERSCORE", "PADDED" -> "_" + number;
            default -> number;
        };
        int dot = node.folder() ? -1 : node.name().lastIndexOf('.');
        return request.getBaseName() + serial + (dot > 0 ? node.name().substring(dot) : "");
    }

    List<Entry> plan(FileOperationRequest request, List<Node> all) {
        return plan(request, all, name -> Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT));
    }

    private List<Entry> plan(FileOperationRequest request, List<Node> all, UnaryOperator<String> weights) {
        return planBetween(request, all, all, false, weights);
    }

    private List<Entry> planBetween(FileOperationRequest request, List<Node> all, List<Node> destinations, boolean cross, UnaryOperator<String> weights) {
        Map<Long, Node> nodes = index(all);
        LinkedHashSet<Long> selected = new LinkedHashSet<>(request.getFileIds());
        if (selected.size() != request.getFileIds().size()) throw new CustomException(400, "选择的文件重复");
        for (Long id : selected) {
            Node node = nodes.get(id);
            if (node == null) throw new CustomException(404, "部分文件已删除或无权操作");
            ancestor(-1L, node.parent(), nodes); // 确认源文件的祖先目录均可用。
        }
        boolean rename = "RENAME".equals(request.getAction());
        List<Node> sources = selected.stream().map(nodes::get).filter(node -> rename || selected.stream()
                .noneMatch(other -> !other.equals(node.id()) && nodes.get(other).folder() && ancestor(other, node.parent(), nodes))).toList();
        if (!rename) {
            ancestor(-1L, request.getTargetParentId(), index(destinations));
            for (Node node : sources) {
                if (!cross && node.folder() && ancestor(node.id(), request.getTargetParentId(), nodes))
                    throw new CustomException(400, "不能复制或移动文件夹到自身或其子目录");
            }
        }
        Map<String, Long> occupied = new HashMap<>();
        for (Node node : destinations) occupied.put(key(node.parent(), node.name(), weights), node.id());
        if (rename && !"SKIP".equals(request.getConflictPolicy()))
            for (Node node : sources) occupied.remove(key(node.parent(), node.name(), weights));
        List<Entry> entries = new ArrayList<>();
        int position = 0;
        for (Node node : sources) {
            position++;
            Long parent = rename ? node.parent() : request.getTargetParentId();
            String name = request.getCustomNames().getOrDefault(node.id(), rename ? renamed(request, node, position) : node.name());
            validName(name);
            String ownKey = key(node.parent(), node.name(), weights);
            if (!"COPY".equals(request.getAction()) && Objects.equals(occupied.get(ownKey), node.id())) occupied.remove(ownKey);
            boolean collision = occupied.containsKey(key(parent, name, weights));
            boolean skipped = collision && "SKIP".equals(request.getConflictPolicy());
            if (collision && "AUTO".equals(request.getConflictPolicy())) {
                String original = name;
                for (int serial = 1; occupied.containsKey(key(parent, name, weights)); serial++) name = numbered(original, node.folder(), serial);
                collision = false;
            }
            boolean unchanged = !"COPY".equals(request.getAction()) && Objects.equals(parent, node.parent()) && name.equals(node.name());
            entries.add(new Entry(node.id(), node.name(), parent, name, collision && !skipped, skipped, unchanged));
            if (skipped && !cross) occupied.put(ownKey, node.id());
            else if (!skipped) occupied.put(key(parent, name, weights), node.id());
        }
        return entries;
    }

    private long copyBytes(FileOperationRequest request, List<Entry> entries, List<Node> nodes) {
        if (!"COPY".equals(request.getAction())) return 0;
        Map<Long, Node> byId = index(nodes);
        long bytes = 0;
        for (Entry entry : entries) {
            if (entry.skipped()) continue;
            for (Node node : nodes) {
                if (!node.folder() && (node.id().equals(entry.fileId()) || ancestor(entry.fileId(), node.parent(), byId)))
                    bytes = Math.addExact(bytes, node.size());
            }
        }
        return bytes;
    }

    private void checkSpace(Long userId, long bytes) {
        if (userId == null || bytes == 0) return;
        Long total = jdbc.queryForObject("SELECT total_space FROM user WHERE user_id = ? FOR UPDATE", Long.class, userId);
        Long used = jdbc.queryForObject("SELECT COALESCE(SUM(file_size), 0) FROM user_file WHERE user_id = ? AND is_folder = 0", Long.class, userId);
        if (total == null || used == null || bytes > total - used) throw new CustomException(400, "存储空间不足，复制文件（含文件夹子项）需要足够的云盘空间");
    }

    private void copyTree(Long userId, Node node, Long parent, String name, List<Node> nodes) {
        if (node.storageId() != null) storage.incrementRefCount(node.storageId());
        if (userId == null) {
            jdbc.update("INSERT INTO public_file (parent_id,file_name,is_folder,storage_id,file_size,file_type,category) VALUES (?,?,?,?,?,?,?)",
                    parent, name, node.folder() ? 1 : 0, node.storageId(), node.size(), fileType(node, name), node.category());
        } else {
            jdbc.update("INSERT INTO user_file (user_id,parent_id,file_name,is_folder,storage_id,file_size,file_type) VALUES (?,?,?,?,?,?,?)",
                    userId, parent, name, node.folder() ? 1 : 0, node.storageId(), node.size(), fileType(node, name));
        }
        Long copiedId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        if (node.folder()) for (Node child : nodes) {
            if (Objects.equals(child.parent(), node.id())) copyTree(userId, child, copiedId, child.name(), nodes);
        }
    }

    private String fileType(Node node, String name) {
        if (node.folder()) return node.type();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(dot + 1).toLowerCase(Locale.ROOT) : "";
    }
}
