package com.huawei.emergency.mapper;

import com.huawei.emergency.entity.EmergencyExecRecord;
import com.huawei.emergency.entity.EmergencyExecRecordExample;
import com.huawei.emergency.entity.EmergencyExecRecordWithBLOBs;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

public interface EmergencyExecRecordMapper {
    long countByExample(EmergencyExecRecordExample example);

    int deleteByExample(EmergencyExecRecordExample example);

    int deleteByPrimaryKey(Integer recordId);

    int insert(EmergencyExecRecordWithBLOBs record);

    int insertSelective(EmergencyExecRecordWithBLOBs record);

    List<EmergencyExecRecordWithBLOBs> selectByExampleWithBLOBs(EmergencyExecRecordExample example);

    List<EmergencyExecRecord> selectByExample(EmergencyExecRecordExample example);

    EmergencyExecRecordWithBLOBs selectByPrimaryKey(Integer recordId);

    int updateByExampleSelective(@Param("record") EmergencyExecRecordWithBLOBs record, @Param("example") EmergencyExecRecordExample example);

    int updateByExampleWithBLOBs(@Param("record") EmergencyExecRecordWithBLOBs record, @Param("example") EmergencyExecRecordExample example);

    int updateByExample(@Param("record") EmergencyExecRecord record, @Param("example") EmergencyExecRecordExample example);

    int updateByPrimaryKeySelective(EmergencyExecRecordWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(EmergencyExecRecordWithBLOBs record);

    int updateByPrimaryKey(EmergencyExecRecord record);

    List<EmergencyExecRecordWithBLOBs> selectAllPlanDetail(Integer planId);

    int tryUpdateStartTime(@Param("recordId") Integer recordId, @Param("startTime") Date startTime);

    int tryUpdateEndTime(@Param("recordId") Integer recordId, @Param("endTime") Date endTime);

    int tryUpdateStatus(Integer recordId);
}