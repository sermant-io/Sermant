package com.huawei.emergency.mapper;

import com.huawei.emergency.entity.EmergencyTask;
import com.huawei.emergency.entity.EmergencyTaskExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EmergencyTaskMapper {
    long countByExample(EmergencyTaskExample example);

    int deleteByExample(EmergencyTaskExample example);

    int deleteByPrimaryKey(Integer taskId);

    int insert(EmergencyTask record);

    int insertSelective(EmergencyTask record);

    List<EmergencyTask> selectByExample(EmergencyTaskExample example);

    EmergencyTask selectByPrimaryKey(Integer taskId);

    int updateByExampleSelective(@Param("record") EmergencyTask record, @Param("example") EmergencyTaskExample example);

    int updateByExample(@Param("record") EmergencyTask record, @Param("example") EmergencyTaskExample example);

    int updateByPrimaryKeySelective(EmergencyTask record);

    long countPassedPlanByTaskId(Integer id);
}