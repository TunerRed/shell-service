package org.shelltest.service.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.shelltest.service.entity.ServiceArgs;
import org.shelltest.service.entity.ServiceArgsExample;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceArgsMapper {
    int countByExample(ServiceArgsExample example);

    int deleteByExample(ServiceArgsExample example);

    int insert(ServiceArgs record);

    int insertSelective(ServiceArgs record);

    List<ServiceArgs> selectByExample(ServiceArgsExample example);
    List<String> getArgsWithDefault(@Param("serverIP") String serverIP, @Param("filename") String filename);
    List<ServiceArgs> getAppNameListWithDefault(@Param("serverIP") String serverIP);

    int updateByExampleSelective(@Param("record") ServiceArgs record, @Param("example") ServiceArgsExample example);

    int updateByExample(@Param("record") ServiceArgs record, @Param("example") ServiceArgsExample example);
}