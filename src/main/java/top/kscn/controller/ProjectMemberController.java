package top.kscn.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.kscn.common.Result;
import top.kscn.entity.ProjectMember;
import top.kscn.entity.dto.ProjectMemberDTO;
import top.kscn.service.ProjectMemberService;

@RestController
@RequestMapping("/api/projectMember")
public class ProjectMemberController {

    @Autowired
    private ProjectMemberService projectMemberService;

    @GetMapping("/auth/projectMemberList")
    public Result<?> projectMemberList(@Param("dto") ProjectMemberDTO dto) {
        IPage<ProjectMemberDTO> result = projectMemberService.projectMemberList(dto);
        return Result.success(null, result);
    }

    @PostMapping("/admin/addProjectMember")
    public Result<?> addProjectMember(@RequestBody ProjectMember projectMember) {
        int result = projectMemberService.addProjectMember(projectMember);
        return result > 0 ? Result.success("添加成功") : Result.error("添加失败");
    }

    @PutMapping("/admin/updateProjectMember")
    public Result<?> updateProjectMember(@RequestBody ProjectMember projectMember) {
        int result = projectMemberService.updateProjectMember(projectMember);
        return result > 0 ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/admin/deleteProjectMember/{projectMemberId}")
    public Result<?> deleteProjectMember(@PathVariable Long projectMemberId) {
        int result = projectMemberService.deleteProjectMember(projectMemberId);
        return result > 0 ? Result.success("删除成功") : Result.error("删除失败");
    }

    @GetMapping("/auth/getProjectMember/{projectMemberId}")
    public Result<?> getProjectMember(@PathVariable Long projectMemberId) {
        ProjectMember projectMember = projectMemberService.getProjectMember(projectMemberId);
        return projectMember != null ? Result.success(null, projectMember) : Result.error("查询失败");
    }
}
