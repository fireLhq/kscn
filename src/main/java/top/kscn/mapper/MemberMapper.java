package top.kscn.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.kscn.entity.Member;

@Mapper
public interface MemberMapper extends BaseMapper<Member> {
}
