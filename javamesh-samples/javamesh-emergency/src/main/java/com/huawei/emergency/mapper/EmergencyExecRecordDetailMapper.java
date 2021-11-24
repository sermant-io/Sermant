package com.huawei.emergency.mapper;

import com.huawei.emergency.entity.EmergencyExecRecordDetail;
import com.huawei.emergency.entity.EmergencyExecRecordDetailExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface EmergencyExecRecordDetailMapper {
    long countByExample(EmergencyExecRecordDetailExample example);

    int deleteByExample(EmergencyExecRecordDetailExample example);

    int deleteByPrimaryKey(Integer detailId);

    int insert(EmergencyExecRecordDetail record);

    int insertSelective(EmergencyExecRecordDetail record);

    List<EmergencyExecRecordDetail> selectByExampleWithBLOBs(EmergencyExecRecordDetailExample example);

    List<EmergencyExecRecordDetail> selectByExample(EmergencyExecRecordDetailExample example);

    EmergencyExecRecordDetail selectByPrimaryKey(Integer detailId);

    int updateByExampleSelective(@Param("record") EmergencyExecRecordDetail record, @Param("example") EmergencyExecRecordDetailExample example);

    int updateByExampleWithBLOBs(@Param("record") EmergencyExecRecordDetail record, @Param("example") EmergencyExecRecordDetailExample example);

    int updateByExample(@Param("record") EmergencyExecRecordDetail record, @Param("example") EmergencyExecRecordDetailExample example);

    int updateByPrimaryKeySelective(EmergencyExecRecordDetail record);

    int updateByPrimaryKeyWithBLOBs(EmergencyExecRecordDetail record);

    int updateByPrimaryKey(EmergencyExecRecordDetail record);
}