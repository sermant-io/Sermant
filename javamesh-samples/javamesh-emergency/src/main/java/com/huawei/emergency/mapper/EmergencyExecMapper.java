/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.mapper;

import com.huawei.emergency.entity.EmergencyExec;
import com.huawei.emergency.entity.EmergencyExecExample;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 预案执行总表mapper
 *
 * @author y30010171
 * @since 2021-11-15
 **/
@Mapper
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