/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.controller;

import com.huawei.common.api.CommonPage;
import com.huawei.common.api.CommonResult;
import com.huawei.common.constant.PlanStatus;
import com.huawei.emergency.dto.PlanQueryDto;
import com.huawei.emergency.dto.PlanQueryParams;
import com.huawei.emergency.dto.PlanSaveParams;
import com.huawei.emergency.dto.TaskNode;
import com.huawei.emergency.entity.EmergencyPlan;
import com.huawei.emergency.entity.User;
import com.huawei.emergency.service.EmergencyPlanService;

import org.apache.commons.lang.StringUtils;
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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


    /**
     * 修改预案下的拓扑任务信息
     *
     * @param request http请求
     * @param params  {@link PlanSaveParams}
     * @return {@link CommonResult}
     */
    @PutMapping("/plan")
    public CommonResult save(HttpServletRequest request, @RequestBody PlanSaveParams params) {
        return planService.save(params.getPlanId(), params.getExpand(), parseUserName(request));
    }

    /**
     * 预案执行
     *
     * @param request http请求
     * @param plan    {@link EmergencyPlan#getPlanId()} 预案ID
     * @return {@link CommonResult}
     */
    @PostMapping("/plan/run")
    public CommonResult run(HttpServletRequest request, @RequestBody EmergencyPlan plan) {
        if (plan.getPlanId() == null) {
            return CommonResult.failed("请选择需要运行的预案");
        }
        return planService.exec(plan.getPlanId(), parseUserName(request));
    }

    /**
     * 预案启动,开始调度
     *
     * @param request http请求
     * @param plan    {@link EmergencyPlan#getPlanId()} 预案ID
     * @return {@link CommonResult}
     */
    @PostMapping("/plan/start")
    public CommonResult start(HttpServletRequest request, @RequestBody EmergencyPlan plan) {
        if (plan.getPlanId() == null) {
            return CommonResult.failed("请选择需要启动的预案");
        }
        return planService.start(plan, parseUserName(request));
    }

    /**
     * 预案停止,停止调度
     *
     * @param request http请求
     * @param plan    {@link EmergencyPlan#getPlanId()} 预案ID
     * @return {@link CommonResult}
     */
    @PostMapping("/plan/stop")
    public CommonResult stop(HttpServletRequest request, @RequestBody EmergencyPlan plan) {
        if (plan.getPlanId() == null) {
            return CommonResult.failed("请选择需要停止的预案");
        }
        return planService.stop(plan.getPlanId(), parseUserName(request));
    }

    /**
     * 查询预案的编号与名称
     *
     * @param planId 预案ID
     * @return {@link CommonResult}
     */
    @GetMapping("/plan/get")
    public CommonResult get(@RequestParam("plan_id") int planId) {
        CommonResult<EmergencyPlan> queryData = planService.get(planId);
        EmergencyPlan plan = queryData.getData();
        EmergencyPlan returnPlan = new EmergencyPlan();
        if (plan != null) {
            returnPlan.setPlanId(plan.getPlanId());
            returnPlan.setPlanNo(plan.getPlanNo());
            returnPlan.setPlanName(plan.getPlanName());
        }
        return CommonResult.success(returnPlan);
    }

    /**
     * 查询预案以及预案下的任务信息
     *
     * @param planName   预案名称或编号
     * @param sceneName  场景名称或编号
     * @param taskName   任务名称或编号
     * @param scriptName 脚本名称
     * @param pageSize   分页大小
     * @param current    当前页码
     * @param sorter     排序字段
     * @param order      排序方式
     * @return {@link CommonResult}
     */
    @GetMapping("/plan")
    public CommonResult queryPlan(@RequestParam(value = "plan_name_no", required = false) String planName,
                                  @RequestParam(value = "scena_name_no", required = false) String sceneName,
                                  @RequestParam(value = "task_name_no", required = false) String taskName,
                                  @RequestParam(value = "script_name", required = false) String scriptName,
                                  @RequestParam(value = "status_label", required = false) String statusLabel,
                                  @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                  @RequestParam(value = "current", defaultValue = "1") int current,
                                  @RequestParam(value = "sorter", defaultValue = "update_time") String sorter,
                                  @RequestParam(value = "order", defaultValue = "DESC") String order) {
        CommonPage<PlanQueryParams> params = new CommonPage<>();
        params.setPageSize(pageSize);
        params.setPageIndex(current);
        params.setSortField(sorter);
        if ("ascend".equals(order)) {
            params.setSortType("ASC");
        } else {
            params.setSortType("DESC");
        }
        PlanQueryParams planParams = new PlanQueryParams();
        planParams.setPlanName(planName);
        planParams.setSceneName(sceneName);
        planParams.setTaskName(taskName);
        planParams.setScriptName(scriptName);
        if (StringUtils.isNotEmpty(statusLabel)) {
            planParams.setStatus(PlanStatus.matchByLabel(statusLabel, PlanStatus.NEW).getValue());
        }
        params.setObject(planParams);
        return planService.plan(params);
    }

    /**
     * 获取预案下的拓扑任务信息
     *
     * @param planId 预案ID
     * @return {@link CommonResult}
     */
    @GetMapping("/plan/task")
    public CommonResult query(@RequestParam("plan_id") int planId) {
        return planService.query(planId);
    }

    /**
     * 新增一个任务
     *
     * @param request  http请求
     * @param taskNode 任务信息
     * @return {@link CommonResult}
     */
    @PostMapping("/plan/task")
    public CommonResult addTask(HttpServletRequest request, @RequestBody TaskNode taskNode) {
        taskNode.setCreateUser(parseUserName(request));
        return planService.addTask(taskNode);
    }

    /**
     * 新增一个预案
     *
     * @param request       http请求
     * @param emergencyPlan {@link EmergencyPlan#getPlanName()} 预案名称
     * @return {@link CommonResult}
     */
    @PostMapping("/plan")
    public CommonResult addPlan(HttpServletRequest request, @RequestBody EmergencyPlan emergencyPlan) {
        emergencyPlan.setCreateUser(parseUserName(request));
        return planService.add(emergencyPlan);
    }

    /**
     * 删除一个预案
     *
     * @param planId 预案ID
     * @return {@link CommonResult}
     */
    @DeleteMapping("/plan")
    public CommonResult deletePlan(@RequestParam("plan_id") int planId) {
        EmergencyPlan plan = new EmergencyPlan();
        plan.setPlanId(planId);
        return planService.delete(plan);
    }

    /**
     * 预案提交审核
     *
     * @param emergencyPlan {@link EmergencyPlan#getPlanId()} 预案ID
     * @return {@link CommonResult}
     */
    @PostMapping("plan/submitReview")
    public CommonResult submitReview(@RequestBody EmergencyPlan emergencyPlan) {
        return planService.submit(emergencyPlan.getPlanId());
    }

    /**
     * 预案审核
     *
     * @param request      http请求
     * @param planQueryDto {@link PlanQueryDto#getPlanId()} 预案ID，{@link PlanQueryDto#getCheckResult()} 审核结果，{@link PlanQueryDto#getCheckResult()} 审核意见
     * @return {@link CommonResult}
     */
    @PostMapping("/plan/approve")
    public CommonResult approve(HttpServletRequest request, @RequestBody PlanQueryDto planQueryDto) {
        EmergencyPlan plan = new EmergencyPlan();
        plan.setPlanId(planQueryDto.getPlanId());
        plan.setStatus(parseCheckResult(planQueryDto.getCheckResult()));
        plan.setCheckRemark(planQueryDto.getComment());
        return planService.approve(plan, parseUserName(request));
    }

    private String parseCheckResult(String checkResult) {
        if ("通过".equals(checkResult)) {
            return PlanStatus.APPROVED.getValue();
        }
        if ("驳回".equals(checkResult)) {
            return PlanStatus.REJECT.getValue();
        }
        return checkResult;
    }

    @GetMapping("/plan/search/status_label")
    public CommonResult planStatus() {
        return CommonResult.success(
            Arrays.stream(PlanStatus.values())
                .map(PlanStatus::getStatusLabel)
                .collect(Collectors.toList()).toArray()
        );
    }

    private String parseUserName(HttpServletRequest request) {
        String userName = "";
        try {
            User user = (User) request.getSession().getAttribute("userInfo");
            if (user != null) {
                userName = user.getUserName();
            }
        } catch (Exception e) {
            LOGGER.error("get user info error.", e);
        }
        return userName;
    }
}
