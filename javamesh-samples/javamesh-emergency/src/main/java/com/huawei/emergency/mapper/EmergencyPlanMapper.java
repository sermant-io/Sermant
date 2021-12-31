/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.mapper;

import com.huawei.emergency.dto.PlanDetailQueryDto;
import com.huawei.emergency.dto.PlanQueryDto;
import com.huawei.emergency.dto.PlanQueryParams;
import com.huawei.emergency.dto.SceneExecDto;
import com.huawei.emergency.entity.EmergencyPlan;
import com.huawei.emergency.entity.EmergencyPlanExample;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 预案mapper
 *
 * @author y30010171
 * @since 2021-11-15
 **/
@Mapper
public interface EmergencyPlanMapper {
    long countByExample(EmergencyPlanExample example);

    int deleteByExample(EmergencyPlanExample example);

    int deleteByPrimaryKey(Integer planId);

    int insert(EmergencyPlan record);

    int insertSelective(EmergencyPlan record);

    List<EmergencyPlan> selectByExample(EmergencyPlanExample example);

    EmergencyPlan selectByPrimaryKey(Integer planId);

    int updateByExampleSelective(@Param("record") EmergencyPlan record, @Param("example") EmergencyPlanExample example);

    int updateByExample(@Param("record") EmergencyPlan record, @Param("example") EmergencyPlanExample example);

    int updateByPrimaryKeySelective(EmergencyPlan record);

    int updateByPrimaryKey(EmergencyPlan record);

    List<PlanQueryDto> queryPlanDto(PlanQueryParams params);

    List<PlanDetailQueryDto> queryPlanDetailDto(Integer id);
}