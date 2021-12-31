/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.service;

import com.huawei.common.api.CommonPage;
import com.huawei.common.api.CommonResult;
import com.huawei.emergency.dto.PlanQueryParams;
import com.huawei.emergency.dto.TaskNode;
import com.huawei.emergency.entity.EmergencyExecRecord;
import com.huawei.emergency.entity.EmergencyPlan;

import java.util.List;

/**
 * 预案管理接口
 *
 * @author y30010171
 * @since 2021-11-02
 **/
public interface EmergencyPlanService extends EmergencyCommonService<EmergencyPlan>, EmergencyCallBack {
    /**
     * 预案执行
     *
     * @param planId   预案ID
     * @param userName 操作人
     * @return {@link CommonResult}
     */
    CommonResult exec(int planId, String userName);

    /**
     * 预案启动
     *
     * @param plan     预案信息
     * @param userName 操作人
     * @return {@link CommonResult}
     */
    CommonResult start(EmergencyPlan plan, String userName);

    /**
     * 预案停止
     *
     * @param planId   预案ID
     * @param userName 操作人
     * @return {@link CommonResult}
     */
    CommonResult stop(int planId, String userName);

    /**
     * 预案审核
     *
     * @param plan {@link EmergencyPlan}
     * @return {@link CommonResult}
     */
    CommonResult approve(EmergencyPlan plan, String userName);

    CommonResult query(int planId);

    CommonResult<EmergencyPlan> get(int planId);

    CommonResult addTask(TaskNode taskNode);

    CommonResult plan(CommonPage<PlanQueryParams> params);

    CommonResult save(int planId, List<TaskNode> listNodes, String userName);

    CommonResult submit(int planId);

    CommonResult copy(EmergencyPlan emergencyPlan);
}
