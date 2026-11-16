package top.kscn.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.springframework.stereotype.Service;
import top.kscn.exception.CustomException;
import top.kscn.utils.PreviewDrawFactory;
import org.apache.poi.sl.draw.Drawable;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class FilePreviewService {
    private final FilePropertiesService files;
    private final Semaphore officeSlots = new Semaphore(2);
    private static final Set<String> TEXT = Set.of("txt", "text", "log", "md", "markdown", "html", "htm", "xml", "json", "csv", "tsv", "yaml", "yml", "toml", "ini", "conf", "cfg", "properties", "java", "c", "cc", "cpp", "cxx", "h", "hpp", "hxx", "py", "js", "jsx", "ts", "tsx", "vue", "css", "scss", "less", "sass", "sql", "sh", "bash", "bat", "cmd", "ps1", "go", "rs", "rb", "php", "kt", "kts", "swift", "scala", "lua", "r", "cs", "tex", "svelte", "ipynb", "gradle", "env");
    public record Content(String name, String kind, String mime, byte[] bytes) {}
    public record Inline(String text, String image, Map<String, String> style) {}
    public record WordCell(List<Block> blocks, int colspan, int rowspan, boolean hidden, Map<String, String> style) {}
    public record Block(String kind, String text, List<List<String>> rows, String image, Map<String, String> style, List<Inline> runs, List<List<WordCell>> cells) {
        public Block(String kind, String text, List<List<String>> rows, String image) { this(kind, text, rows, image, Map.of(), List.of(), List.of()); }
    }
    public record ExcelCell(String text, Map<String, String> style, Map<String, String> textStyle, int colspan, int rowspan, boolean hidden) {}
    public record SheetView(String name, List<List<String>> rows, List<List<ExcelCell>> cells, List<Double> columnWidths, List<Double> rowHeights) {
        public SheetView(String name, List<List<String>> rows) { this(name, rows, List.of(), List.of(), List.of()); }
    }
    public record OfficeView(String kind, List<Block> blocks, List<SheetView> sheets, List<String> slides, boolean truncated, String notice, Map<String, String> pageStyle) {
        public OfficeView(String kind, List<Block> blocks, List<SheetView> sheets, List<String> slides, boolean truncated, String notice) { this(kind, blocks, sheets, slides, truncated, notice, Map.of()); }
    }

    static {
        ZipSecureFile.setMaxEntrySize(20L * 1024 * 1024);
        ZipSecureFile.setMaxTextSize(5L * 1024 * 1024);
        ZipSecureFile.setMaxFileCount(2000);
    }

    public static String extension(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
    public static String kind(String name) {
        String ext = extension(name);
        if (Set.of("png", "jpg", "jpeg", "gif", "webp", "bmp", "svg", "avif", "ico").contains(ext)) return "image";
        if (Set.of("mp4", "webm", "ogv", "mov", "m4v", "mkv", "avi").contains(ext)) return "video";
        if (Set.of("mp3", "wav", "ogg", "oga", "m4a", "aac", "flac", "opus").contains(ext)) return "audio";
        if (ext.equals("pdf")) return "pdf";
        if (Set.of("doc", "docx").contains(ext)) return "word";
        if (Set.of("xls", "xlsx").contains(ext)) return "excel";
        if (Set.of("ppt", "pptx").contains(ext)) return "ppt";
        if (TEXT.contains(ext) || Set.of("dockerfile", "makefile", "license", "readme", ".gitignore", ".env").contains(name.toLowerCase(Locale.ROOT))) return "text";
        throw new CustomException(415, "暂不支持此文件格式的预览，请下载后查看");
    }
    private static String mime(String name, String kind) {
        String ext = extension(name);
        if (kind.equals("text")) return "text/plain"; // 原始HTML/XML绝不作为主站活动页面返回。
        if (kind.equals("pdf")) return "application/pdf";
        return switch (ext) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "svg" -> "image/svg+xml";
            case "ico" -> "image/x-icon";
            case "mp4", "m4v" -> "video/mp4";
            case "mov" -> "video/quicktime";
            case "mkv" -> "video/x-matroska";
            case "avi" -> "video/x-msvideo";
            case "ogv" -> "video/ogg";
            case "mp3" -> "audio/mpeg";
            case "m4a" -> "audio/mp4";
            case "ogg", "oga", "opus" -> "audio/ogg";
            default -> kind + "/" + ext;
        };
    }
    public Content content(Long userId, Long fileId) {
        var metadata = files.properties(userId, fileId);
        if (metadata.fileSize() >= FilePropertiesService.PREVIEW_LIMIT) throw new CustomException(413, "文件过大，仅支持小于10MB的文件，请下载后预览");
        String kind = kind(metadata.fileName());
        if (Set.of("word", "excel", "ppt").contains(kind)) throw new CustomException(400, "请使用Office文档预览接口");
        var source = files.previewSource(userId, fileId);
        if (!kind(source.name()).equals(kind)) throw new CustomException(409, "文件名称已变化，请重新预览");
        return new Content(source.name(), kind, mime(source.name(), kind), source.bytes());
    }

    public OfficeView office(Long userId, Long fileId) {
        var metadata = files.properties(userId, fileId);
        if (metadata.fileSize() >= FilePropertiesService.PREVIEW_LIMIT) throw new CustomException(413, "文件过大，请下载后预览");
        if (!Set.of("word", "excel", "ppt").contains(kind(metadata.fileName()))) throw new CustomException(415, "此文件不是支持的Office格式");
        if (!officeSlots.tryAcquire()) throw new CustomException(429, "预览任务较多，请稍后重试");
        try {
            var source = files.previewSource(userId, fileId);
            String kind = kind(source.name());
            if (!Set.of("word", "excel", "ppt").contains(kind)) throw new CustomException(409, "文件已变化，请重新预览");
            checkArchive(source.bytes());
            return switch (kind) {
                case "word" -> WordStylePreview.render(source);
                case "excel" -> ExcelStylePreview.render(source.bytes());
                default -> slides(source.bytes());
            };
        } catch (CustomException exception) { throw exception; }
        catch (Exception exception) { throw new CustomException(422, "无法预览此文档，文件可能已加密、损坏或包含不支持的内容，请下载后查看"); }
        finally { officeSlots.release(); }
    }

    private void checkArchive(byte[] bytes) throws IOException {
        if (bytes.length < 2 || bytes[0] != 'P' || bytes[1] != 'K') return;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            long total = 0; int entries = 0; byte[] buffer = new byte[8192];
            while (zip.getNextEntry() != null) {
                if (++entries > 2000) throw new CustomException(422, "文档结构过于复杂，请下载后查看");
                long size = 0;
                for (int count; (count = zip.read(buffer)) != -1;) {
                    size += count; total += count;
                    if (size > 20L * 1024 * 1024 || total > 50L * 1024 * 1024)
                        throw new CustomException(422, "文档解压内容过大，请下载后查看");
                }
            }
        }
    }


    private OfficeView slides(byte[] data) throws IOException {
        List<String> images = new ArrayList<>(); boolean truncated; long totalBytes = 0;
        try (SlideShow<?, ?> show = SlideShowFactory.create(new ByteArrayInputStream(data))) {
            var size = show.getPageSize();
            if (size.width <= 0 || size.height <= 0) throw new CustomException(422, "幻灯片尺寸无效，请下载后查看");
            double scale = Math.min(960.0 / size.width, 1440.0 / size.height);
            int width = Math.max(1, (int) Math.ceil(size.width * scale)), height = Math.max(1, (int) Math.ceil(size.height * scale));
            truncated = show.getSlides().size() > 30;
            for (var slide : show.getSlides()) {
                if (images.size() >= 30 || totalBytes >= 15 * 1024 * 1024) { truncated = true; break; }
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = image.createGraphics();
                try {
                    graphics.setColor(Color.WHITE); graphics.fillRect(0, 0, width, height);
                    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    graphics.setRenderingHint(Drawable.DRAW_FACTORY, new PreviewDrawFactory());
                    graphics.scale(scale, scale); slide.draw(graphics);
                } finally { graphics.dispose(); }
                ByteArrayOutputStream output = new ByteArrayOutputStream(); ImageIO.write(image, "png", output); image.flush();
                totalBytes += output.size(); images.add("data:image/png;base64," + Base64.getEncoder().encodeToString(output.toByteArray()));
            }
        }
        return new OfficeView("ppt", List.of(), List.of(), images, truncated, "静态幻灯片预览，不播放动画、嵌入媒体；缺少字体时排版可能不同。");
    }
}
