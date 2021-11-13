/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.controller;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.huawei.common.api.CommonPage;
import com.huawei.common.api.CommonResult;
import com.huawei.emergency.dto.PlanQueryParams;
import com.huawei.emergency.dto.PlanSaveParams;
import com.huawei.emergency.dto.TaskNode;
import com.huawei.emergency.entity.EmergencyExecRecord;
import com.huawei.emergency.entity.EmergencyPlan;
import com.huawei.emergency.mapper.EmergencyExecMapper;
import com.huawei.emergency.service.EmergencyPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * 预案管理controller
 *
 * @author y30010171
 * @since 2021-11-02
 **/
@RestController
@RequestMapping("/api")
public class EmergencyPlanController {

    @Autowired
    private EmergencyPlanService planService;

    @Autowired
    private EmergencyExecMapper execMapper;


    @PutMapping("/plan")
    public CommonResult save(@RequestBody PlanSaveParams params) {
        return planService.save(params.getPlanId(), params.getExpand());
    }

    /**
     * 预案执行
     *
     * @param plan {@link EmergencyPlan} 预案ID
     * @return {@link CommonResult}
     */
    @PostMapping("/plan/run")
    public CommonResult run(@RequestBody EmergencyPlan plan) {
        if (plan.getPlanId() == null){
            return CommonResult.failed("请选择需要运行的预案");
        }
        return planService.exec(plan.getPlanId());
    }

    /**
     * 获取预案以及预案下的任务信息
     *
     * @param params
     * @return {@link CommonResult}
     */
    @GetMapping("/plan")
    public CommonResult queryPlan(@RequestParam(value = "plan_name_no", required = false) String planName,
                                  @RequestParam(value = "scene_name_no", required = false) String sceneName,
                                  @RequestParam(value = "task_name_no",required = false) String taskName,
                                  @RequestParam(value = "script_name_no",required = false) String scriptName,
                                  @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                  @RequestParam(value = "current", defaultValue = "1") int current,
                                  @RequestParam(value = "sorter", defaultValue = "create_time") String sorter,
                                  @RequestParam(value = "order", defaultValue = "DESC") String order) {
        CommonPage<PlanQueryParams> params = new CommonPage<>();
        params.setPageSize(pageSize);
        params.setPageIndex(current);
        params.setSortField(sorter);
        params.setSortType(order);
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
    public CommonResult addTask(@RequestBody TaskNode taskNode) {
        return planService.addTask(taskNode);
    }

    /**
     * 新增预案
     *
     * @param emergencyPlan {@link EmergencyPlan} 预案信息
     * @return {@link CommonResult} 可以通过{@link CommonResult#getData()}获取新增后的预案信息
     */
    @PostMapping("/plan")
    public CommonResult addPlan(@RequestBody EmergencyPlan emergencyPlan) {
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
    public CommonResult approve(@RequestBody EmergencyPlan emergencyPlan) {
        emergencyPlan.setCheckResult(parseCheckResult(emergencyPlan.getCheckResult()));
        return planService.approve(emergencyPlan);
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

    /**
     * 预案的执行记录
     *
     * @param params
     * @return
     */
    @GetMapping("/record")
    public CommonResult allPlanExecRecords(@RequestBody CommonPage<EmergencyPlan> params) {
        return planService.allPlanExecRecords(params);
    }

    /**
     * 查询某条预案执行记录下的场景执行明细
     *
     * @param params
     * @return
     */
    @GetMapping("/scene/record")
    public CommonResult allSceneExecRecords(@RequestBody CommonPage<EmergencyExecRecord> params) {
        return planService.allSceneExecRecords(params);
    }

    /**
     * 查询某条场景执行明细下的任务执行明细
     *
     * @param params
     * @return
     */
    @GetMapping("/task/record")
    public CommonResult allTaskExecRecords(@RequestBody CommonPage<EmergencyExecRecord> params) {
        return planService.allTaskExecRecords(params);
    }

    /**
     * 执行记录下载
     *
     * @param response
     */
    @GetMapping("/download")
    public void download(HttpServletResponse response) {
        ExcelWriter excelWriter = null;
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            List<Map> allRecords = execMapper.allRecords();
            String fileName = "exec_records.xlsx";
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
            response.setHeader("Content-Disposition",
                "attachment;fileName=" + new String(URLEncoder.encode(fileName, "UTF-8").getBytes("UTF-8")));
            excelWriter = ExcelUtil.getBigWriter();
            excelWriter.renameSheet("执行记录汇总").write(allRecords);
            excelWriter.flush(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (excelWriter != null) {
                excelWriter.close();
            }
        }
    }
}
