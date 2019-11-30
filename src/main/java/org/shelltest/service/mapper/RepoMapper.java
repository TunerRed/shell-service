package org.shelltest.service.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.shelltest.service.entity.Repo;
import org.shelltest.service.entity.RepoExample;
import org.springframework.stereotype.Repository;

@Repository
public interface RepoMapper {
    int countByExample(RepoExample example);

    int deleteByExample(RepoExample example);

    int insert(Repo record);

    int insertSelective(Repo record);

    List<Repo> selectByExample(RepoExample example);

    int updateByExampleSelective(@Param("record") Repo record, @Param("example") RepoExample example);

    int updateByExample(@Param("record") Repo record, @Param("example") RepoExample example);
}