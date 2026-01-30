package top.kscn.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.kscn.entity.ProjectMember;
import top.kscn.entity.dto.ProjectMemberDTO;

@Mapper
public interface ProjectMemberMapper extends BaseMapper<ProjectMember> {
    IPage<ProjectMemberDTO> projectMemberList(Page<?> page, @Param("dto") ProjectMemberDTO dto);
}