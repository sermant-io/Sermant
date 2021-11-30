/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.mapper;

import com.huawei.emergency.dto.TaskNode;
import com.huawei.emergency.entity.EmergencyPlanDetail;
import com.huawei.emergency.entity.EmergencyPlanDetailExample;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预案与任务关系mapper
 *
 * @author y30010171
 * @since 2021-11-15
 **/
@Mapper
public interface EmergencyPlanDetailMapper {
    long countByExample(EmergencyPlanDetailExample example);

    int deleteByExample(EmergencyPlanDetailExample example);

    int deleteByPrimaryKey(Integer detailId);

    int insert(EmergencyPlanDetail record);

    int insertSelective(EmergencyPlanDetail record);

    List<EmergencyPlanDetail> selectByExample(EmergencyPlanDetailExample example);

    EmergencyPlanDetail selectByPrimaryKey(Integer detailId);

    int updateByExampleSelective(@Param("record") EmergencyPlanDetail record,
                                 @Param("example") EmergencyPlanDetailExample example);

    int updateByExample(@Param("record") EmergencyPlanDetail record,
                        @Param("example") EmergencyPlanDetailExample example);

    int updateByPrimaryKeySelective(EmergencyPlanDetail record);

    int updateByPrimaryKey(EmergencyPlanDetail record);

    List<TaskNode> selectSceneNodeByPlanId(Integer planId);

    List<TaskNode> selectTaskNodeBySceneId(Integer planId,Integer sceneId);

    List<TaskNode> selectTaskNodeByTaskId(Integer planId,Integer sceneId,Integer taskId);
}