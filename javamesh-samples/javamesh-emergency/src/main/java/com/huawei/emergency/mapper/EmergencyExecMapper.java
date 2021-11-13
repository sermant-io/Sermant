package com.huawei.emergency.mapper;

import com.huawei.emergency.entity.EmergencyExec;
import com.huawei.emergency.entity.EmergencyExecExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface EmergencyExecMapper {
    long countByExample(EmergencyExecExample example);

    int deleteByExample(EmergencyExecExample example);

    int deleteByPrimaryKey(Integer execId);

    int insert(EmergencyExec record);

    int insertSelective(EmergencyExec record);

    List<EmergencyExec> selectByExample(EmergencyExecExample example);

    EmergencyExec selectByPrimaryKey(Integer execId);

    int updateByExampleSelective(@Param("record") EmergencyExec record, @Param("example") EmergencyExecExample example);

    int updateByExample(@Param("record") EmergencyExec record, @Param("example") EmergencyExecExample example);

    int updateByPrimaryKeySelective(EmergencyExec record);

    int updateByPrimaryKey(EmergencyExec record);

    List<Map> allRecords();
}