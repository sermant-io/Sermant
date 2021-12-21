/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.mapper;

import com.huawei.emergency.entity.EmergencyTask;
import com.huawei.emergency.entity.EmergencyTaskExample;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 任务mapper
 *
 * @author y30010171
 * @since 2021-11-15
 **/
@Mapper
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

    int updateByPrimaryKey(EmergencyTask record);

    long countPassedPlanByTaskId(Integer id);

    int tryClearTaskNo(Integer planId);

    int selectMaxSubTaskNo(String preTaskNo);
}