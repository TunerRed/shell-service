package org.shelltest.service.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.shelltest.service.dto.StatisticEntity;
import org.shelltest.service.entity.History;
import org.shelltest.service.entity.HistoryExample;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryMapper {
    int countByExample(HistoryExample example);

    int deleteByExample(HistoryExample example);

    int deleteByPrimaryKey(Integer messageId);

    int insert(History record);

    int insertSelective(History record);

    List<History> selectByExampleWithBLOBs(HistoryExample example);

    List<History> selectByExample(HistoryExample example);

    History selectByPrimaryKey(Integer messageId);

    List<History> selectNotRead(Integer limit);
    List<History> selectAlreadyRead(Integer limit);
    List<History> selectMessage(Integer limit);

    List<StatisticEntity> getStatisticList(@Param("from")String from, @Param("to")String to);

    int updateByExampleSelective(@Param("record") History record, @Param("example") HistoryExample example);

    int updateByExampleWithBLOBs(@Param("record") History record, @Param("example") HistoryExample example);

    int updateByExample(@Param("record") History record, @Param("example") HistoryExample example);

    int updateByPrimaryKeySelective(History record);

    int updateByPrimaryKeyWithBLOBs(History record);

    int updateByPrimaryKey(History record);
}
