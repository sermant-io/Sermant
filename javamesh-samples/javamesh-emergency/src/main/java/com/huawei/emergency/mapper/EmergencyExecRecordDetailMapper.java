/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.mapper;

import com.huawei.emergency.entity.EmergencyExecRecordDetail;
import com.huawei.emergency.entity.EmergencyExecRecordDetailExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 预案执行记录明细mapper
 *
 * @author y30010171
 * @since 2021-11-15
 **/
@Mapper
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

    List<EmergencyExecRecordDetail> selectAllServerDetail(int recordId);
}