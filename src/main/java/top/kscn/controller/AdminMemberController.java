package top.kscn.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.kscn.common.Result;
import top.kscn.entity.Member;
import top.kscn.entity.dto.member.MemberDTO;
import top.kscn.service.MemberService;

@RestController
@RequestMapping("/api/admin/member")
public class AdminMemberController {

    @Autowired
    private MemberService memberService;

    @GetMapping
    public Result<?> getAdminMemberPage(MemberDTO dto,
                                        @RequestParam(required = false) Long page,
                                        @RequestParam(required = false) Long size) {
        Page<Member> pageData = memberService.getMemberPage(dto, page, size);
        return Result.success(null, pageData);
    }

    @PostMapping
    public Result<?> postMember(@RequestBody @Validated(MemberDTO.CreateGroup.class) MemberDTO dto) {
        memberService.postMember(dto);
        return Result.success("添加成功", null);
    }

    @PutMapping("/{memberId}")
    public Result<?> putMember(@PathVariable Long memberId,
                               @RequestBody @Validated(MemberDTO.UpdateGroup.class) MemberDTO dto) {
        memberService.putMember(memberId, dto);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{memberId}")
    public Result<?> deleteMember(@PathVariable Long memberId) {
        memberService.deleteMember(memberId);
        return Result.success("删除成功", null);
    }

    @PostMapping("/{memberId}/avatar")
    public Result<?> postMemberAvatar(@PathVariable Long memberId,
                                      @RequestParam("file") MultipartFile file) throws Exception {
        String newFileName = memberService.postMemberAvatar(memberId, file);
        return Result.success("头像上传成功", newFileName);
    }

}

