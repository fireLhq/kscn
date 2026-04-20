package top.kscn.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import top.kscn.entity.Member;
import top.kscn.entity.dto.member.MemberDTO;

public interface MemberService extends IService<Member> {

    Page<Member> getMemberPage(MemberDTO dto, Long page, Long size);

    Member getMember(Long memberId);

    void postMember(MemberDTO dto);

    void putMember(Long memberId, MemberDTO dto);

    void deleteMember(Long memberId);

    String postMemberAvatar(Long memberId, MultipartFile file) throws Exception;

    void getMemberAvatar(String filename, HttpServletResponse response);
}
