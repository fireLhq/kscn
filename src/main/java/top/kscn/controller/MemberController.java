package top.kscn.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.kscn.common.Result;
import top.kscn.entity.Member;
import top.kscn.entity.dto.member.MemberDTO;
import top.kscn.exception.CustomException;
import top.kscn.service.MemberService;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @GetMapping
    public Result<?> getMemberPage(MemberDTO dto,
                                   @RequestParam(required = false) Long page,
                                   @RequestParam(required = false) Long size) {
        Page<Member> pageData = memberService.getMemberPage(dto, page, size);
        return Result.success(null, pageData);
    }

    @GetMapping("/{memberId}")
    public Result<?> getMember(@PathVariable Long memberId) {
        Member member = memberService.getMember(memberId);
        if (member == null) throw new CustomException(404, "成员不存在");
        return Result.success(null, member);
    }

    @GetMapping("/avatar/{filename}")
    public void getMemberAvatar(@PathVariable String filename, HttpServletResponse response) {
        memberService.getMemberAvatar(filename, response);
    }
}
