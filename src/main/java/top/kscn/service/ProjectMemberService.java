package top.kscn.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import top.kscn.entity.ProjectMember;
import top.kscn.entity.dto.ProjectMemberDTO;

public interface ProjectMemberService extends IService<ProjectMember> {
    IPage<ProjectMemberDTO> projectMemberList(ProjectMemberDTO dto);
    int addProjectMember(ProjectMember projectMember);
    int updateProjectMember(ProjectMember projectMember);
    int deleteProjectMember(Long projectMemberId);
    ProjectMember getProjectMember(Long projectMemberId);
}
