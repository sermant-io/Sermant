package com.huawei.emergency.mapper;

import com.huawei.emergency.dto.TaskNode;
import com.huawei.emergency.entity.EmergencyPlanDetail;
import com.huawei.emergency.entity.EmergencyPlanDetailExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EmergencyPlanDetailMapper {
    long countByExample(EmergencyPlanDetailExample example);

    int deleteByExample(EmergencyPlanDetailExample example);

    int deleteByPrimaryKey(Integer detailId);

    int insert(EmergencyPlanDetail record);

    int insertSelective(EmergencyPlanDetail record);

    List<EmergencyPlanDetail> selectByExample(EmergencyPlanDetailExample example);

    EmergencyPlanDetail selectByPrimaryKey(Integer detailId);

    int updateByExampleSelective(@Param("record") EmergencyPlanDetail record, @Param("example") EmergencyPlanDetailExample example);

    int updateByExample(@Param("record") EmergencyPlanDetail record, @Param("example") EmergencyPlanDetailExample example);

    int updateByPrimaryKeySelective(EmergencyPlanDetail record);

    int updateByPrimaryKey(EmergencyPlanDetail record);

    List<TaskNode> selectSceneNodeByPlanId(Integer planId);

    List<TaskNode> selectTaskNodeBySceneId(Integer planId,Integer sceneId);

    List<TaskNode> selectTaskNodeByTaskId(Integer planId,Integer sceneId,Integer taskId);
}