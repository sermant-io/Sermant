/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.controller;

import com.huawei.common.api.CommonPage;
import com.huawei.common.api.CommonResult;
import com.huawei.emergency.dto.PlanQueryParams;
import com.huawei.emergency.dto.PlanSaveParams;
import com.huawei.emergency.dto.TaskNode;
import com.huawei.emergency.entity.EmergencyPlan;
import com.huawei.emergency.entity.User;
import com.huawei.emergency.service.EmergencyPlanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 预案管理controller
 *
 * @author y30010171
 * @since 2021-11-02
 **/
@RestController
@RequestMapping("/api")
public class EmergencyPlanController {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmergencyPlanController.class);

    @Autowired
    private EmergencyPlanService planService;


    @PutMapping("/plan")
    public CommonResult save(HttpServletRequest request, @RequestBody PlanSaveParams params) {
        String userName = "";
        try {
            User user = (User) request.getSession().getAttribute("userInfo");
            if (user != null) {
                userName = user.getUserName();
            }
        } catch (Exception e) {
            LOGGER.error("get user info error.", e);
        }
        return planService.save(params.getPlanId(), params.getExpand(), userName);
    }

    /**
     * 预案执行
     *
     * @param plan {@link EmergencyPlan} 预案ID
     * @return {@link CommonResult}
     */
    @PostMapping("/plan/run")
    public CommonResult run(HttpServletRequest request, @RequestBody EmergencyPlan plan) {
        String userName = "";
        try {
            User user = (User) request.getSession().getAttribute("userInfo");
            if (user != null) {
                userName = user.getUserName();
            }
        } catch (Exception e) {
            LOGGER.error("get user info error.", e);
        }
        if (plan.getPlanId() == null) {
            return CommonResult.failed("请选择需要运行的预案");
        }
        return planService.exec(plan.getPlanId(), userName);
    }

    /**
     * 获取预案以及预案下的任务信息
     *
     * @return {@link CommonResult}
     */
    @GetMapping("/plan")
    public CommonResult queryPlan(@RequestParam(value = "plan_name_no", required = false) String planName,
                                  @RequestParam(value = "scena_name_no", required = false) String sceneName,
                                  @RequestParam(value = "task_name_no", required = false) String taskName,
                                  @RequestParam(value = "script_name", required = false) String scriptName,
                                  @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                  @RequestParam(value = "current", defaultValue = "1") int current,
                                  @RequestParam(value = "sorter", defaultValue = "create_time") String sorter,
                                  @RequestParam(value = "order", defaultValue = "DESC") String order) {
        CommonPage<PlanQueryParams> params = new CommonPage<>();
        params.setPageSize(pageSize);
        params.setPageIndex(current);
        params.setSortField(sorter);
        if (order.equals("ascend")) {
            params.setSortType("ASC");
        } else {
            params.setSortType("DESC");
        }
        PlanQueryParams planParams = new PlanQueryParams();
        planParams.setPlanName(planName);
        planParams.setSceneName(sceneName);
        planParams.setTaskName(taskName);
        planParams.setScriptName(scriptName);
        params.setObject(planParams);
        return planService.plan(params);
    }

    /**
     * 获取预案下的拓扑任务图
     *
     * @param planId
     * @return {@link CommonResult}
     */
    @GetMapping("/plan/task")
    public CommonResult query(@RequestParam("plan_id") int planId) {
        return planService.query(planId);
    }

    /**
     * 获取预案下的拓扑任务图
     *
     * @param taskNode 新增任务
     * @return {@link CommonResult}
     */
    @PostMapping("/plan/task")
    public CommonResult addTask(HttpServletRequest request, @RequestBody TaskNode taskNode) {
        try {
            User user = (User) request.getSession().getAttribute("userInfo");
            if (user != null) {
                taskNode.setCreateUser(user.getUserName());
            }
        } catch (Exception e) {
            LOGGER.error("get user info error.", e);
        }
        return planService.addTask(taskNode);
    }

    /**
     * 新增预案
     *
     * @param emergencyPlan {@link EmergencyPlan} 预案信息
     * @return {@link CommonResult} 可以通过{@link CommonResult#getData()}获取新增后的预案信息
     */
    @PostMapping("/plan")
    public CommonResult addPlan(HttpServletRequest request, @RequestBody EmergencyPlan emergencyPlan) {
        try {
            User user = (User) request.getSession().getAttribute("userInfo");
            if (user != null) {
                emergencyPlan.setCreateUser(user.getUserName());
            }
        } catch (Exception e) {
            LOGGER.error("get user info error.", e);
        }
        return planService.add(emergencyPlan);
    }

    /**
     * 删除预案
     *
     * @param planId
     * @return
     */
    @DeleteMapping("/plan")
    public CommonResult deletePlan(@RequestParam("plan_id") int planId) {
        EmergencyPlan plan = new EmergencyPlan();
        plan.setPlanId(planId);
        return planService.delete(plan);
    }

    /**
     * 预案提审
     *
     * @param emergencyPlan
     * @return
     */
    @PostMapping("plan/submitReview")
    public CommonResult submitReview(@RequestBody EmergencyPlan emergencyPlan) {
        return planService.submit(emergencyPlan.getPlanId());
    }

    /**
     * 预案审核
     *
     * @param emergencyPlan
     * @return
     */
    @PostMapping("/plan/approve")
    public CommonResult approve(HttpServletRequest request, @RequestBody EmergencyPlan emergencyPlan) {
        String userName = "";
        try {
            User user = (User) request.getSession().getAttribute("userInfo");
            if (user != null) {
                userName = user.getUserName();
            }
        } catch (Exception e) {
            LOGGER.error("get user info error.", e);
        }
        emergencyPlan.setCheckResult(parseCheckResult(emergencyPlan.getCheckResult()));
        return planService.approve(emergencyPlan, userName);
    }

    private String parseCheckResult(String checkResult) {
        if ("通过".equals(checkResult)) {
            return "2";
        }
        if ("驳回".equals(checkResult)) {
            return "3";
        }
        return checkResult;
    }
}
