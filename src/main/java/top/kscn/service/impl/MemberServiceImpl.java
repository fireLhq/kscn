package top.kscn.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.kscn.entity.Member;
import top.kscn.entity.dto.member.MemberDTO;
import top.kscn.exception.CustomException;
import top.kscn.mapper.MemberMapper;
import top.kscn.service.MemberService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {

    @Value("${file.storage-root}")
    private String storageRoot;

    @Override
    public Page<Member> getMemberPage(MemberDTO dto, Long page, Long size) {
        long pageNo = (page == null || page < 1) ? 1L : page;
        long pageSize = (size == null || size < 1) ? 10L : size;
        Page<Member> pageData = new Page<>(pageNo, pageSize);
        return page(pageData, Wrappers.<Member>lambdaQuery()
                .like(StringUtils.isNotBlank(dto.getName()), Member::getName, dto.getName())
                .like(StringUtils.isNotBlank(dto.getRoleName()), Member::getRoleName, dto.getRoleName())
                .eq(dto.getMemberType() != null, Member::getMemberType, dto.getMemberType())
                .orderByAsc(Member::getSort)
                .orderByAsc(Member::getId));
    }

    @Override
    public Member getMember(Long memberId) {
        return getById(memberId);
    }

    @Override
    public void postMember(MemberDTO dto) {
        Member member = new Member();
        BeanUtils.copyProperties(dto, member, "id", "avatar", "createTime", "updateTime");
        if (member.getSort() == null) {
            member.setSort(0);
        }
        if (!save(member)) throw new CustomException(500, "添加成员失败");
    }

    @Override
    public void putMember(Long memberId, MemberDTO dto) {
        Member oldMember = getById(memberId);
        if (oldMember == null) throw new CustomException(404, "成员不存在");

        Member member = new Member();
        BeanUtils.copyProperties(dto, member, "id", "avatar", "createTime", "updateTime");
        member.setId(memberId);
        if (member.getSort() == null) {
            member.setSort(oldMember.getSort());
        }
        if (!updateById(member)) throw new CustomException(500, "更新成员失败");
    }

    @Override
    public void deleteMember(Long memberId) {
        Member member = getById(memberId);
        if (member == null) throw new CustomException(404, "成员不存在");

        if (member.getAvatar() != null && !member.getAvatar().isBlank() && !"default_member_avatar.png".equals(member.getAvatar())) {
            try {
                Files.deleteIfExists(Paths.get(storageRoot, "member", "avatar", member.getAvatar()));
            } catch (Exception ignored) {
            }
        }
        if (!removeById(memberId)) throw new CustomException(500, "删除成员失败");
    }

    @Override
    public String postMemberAvatar(Long memberId, MultipartFile file) throws Exception {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException(400, "只能上传图片文件");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new CustomException(400, "头像文件大小不能超过10MB");
        }

        Member member = getById(memberId);
        if (member == null) throw new CustomException(404, "成员不存在");

        Path avatarDir = Paths.get(storageRoot, "member", "avatar");
        Files.createDirectories(avatarDir);

        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new CustomException(400, "无法读取图片文件，请确保上传的是有效的图片");
        }

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        int side = Math.min(width, height);
        int x = (width - side) / 2;
        int y = (height - side) / 2;
        BufferedImage squareImage = originalImage.getSubimage(x, y, side, side);

        String oldAvatar = member.getAvatar();
        String newFileName = memberId + ".png";
        Path avatarPath = avatarDir.resolve(newFileName);
        Thumbnails.of(squareImage).size(256, 256).outputFormat("png").toFile(avatarPath.toFile());

        if (oldAvatar != null && !oldAvatar.isBlank() && !"default_member_avatar.png".equals(oldAvatar) && !oldAvatar.equals(newFileName)) {
            try {
                Files.deleteIfExists(avatarDir.resolve(oldAvatar));
            } catch (Exception ignored) {
            }
        }

        Member updateMember = new Member();
        updateMember.setId(memberId);
        updateMember.setAvatar(newFileName);
        if (!updateById(updateMember)) throw new CustomException(500, "成员头像上传失败");
        return newFileName;
    }

    @Override
    public void getMemberAvatar(String filename, HttpServletResponse response) {
        try {
            Path avatarPath = Paths.get(storageRoot, "member", "avatar", filename);
            File avatarFile = avatarPath.toFile();
            if (!avatarFile.exists()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "头像不存在");
                return;
            }
            String contentType = Files.probeContentType(avatarPath);
            response.setContentType(contentType != null ? contentType : "image/png");
            response.setHeader("Cache-Control", "max-age=86400");
            response.setContentLengthLong(avatarFile.length());
            try (InputStream in = new BufferedInputStream(new FileInputStream(avatarFile));
                 OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }
        } catch (Exception e) {
            throw new CustomException(500, "获取成员头像失败");
        }
    }
}
