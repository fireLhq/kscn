package top.kscn.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.kscn.entity.ProjectMember;
import top.kscn.entity.dto.ProjectMemberDTO;
import top.kscn.mapper.ProjectMemberMapper;
import top.kscn.service.ProjectMemberService;

@Service
public class ProjectMemberServiceImpl extends ServiceImpl<ProjectMemberMapper, ProjectMember>
        implements ProjectMemberService {

    @Autowired
    private ProjectMemberMapper projectMemberMapper;

    @Override
    public IPage<ProjectMemberDTO> projectMemberList(ProjectMemberDTO dto) {
        Page<ProjectMemberDTO> page = new Page<>(dto.getPage(), dto.getSize());
        return projectMemberMapper.projectMemberList(page, dto);
    }

    @Override
    public int addProjectMember(ProjectMember projectMember) {
        return projectMemberMapper.insert(projectMember);
    }

    @Override
    public int updateProjectMember(ProjectMember projectMember) {
        return projectMemberMapper.updateById(projectMember);
    }

    @Override
    public int deleteProjectMember(Long projectMemberId) {
        return projectMemberMapper.deleteById(projectMemberId);
    }

    @Override
    public ProjectMember getProjectMember(Long projectMemberId) {
        return projectMemberMapper.selectById(projectMemberId);
    }
}
