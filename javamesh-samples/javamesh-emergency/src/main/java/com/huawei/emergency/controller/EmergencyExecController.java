/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.controller;

import com.huawei.common.api.CommonPage;
import com.huawei.common.api.CommonResult;
import com.huawei.emergency.dto.PlanQueryDto;
import com.huawei.emergency.entity.EmergencyExecRecord;
import com.huawei.emergency.entity.EmergencyPlan;
import com.huawei.emergency.entity.User;
import com.huawei.emergency.mapper.EmergencyExecMapper;
import com.huawei.emergency.service.EmergencyExecService;
import com.huawei.emergency.service.EmergencyPlanService;
import com.huawei.script.exec.log.LogResponse;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 执行记录管理
 *
 * @author y30010171
 * @since 2021-11-09
 **/
@RestController
@RequestMapping("/api")
public class EmergencyExecController {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmergencyExecController.class);

    @Autowired
    private EmergencyPlanService planService;

    @Autowired
    private EmergencyExecService execService;

    @Autowired
    private EmergencyExecMapper execMapper;

    /**
     * 重新执行某条失败的执行记录
     *
     * @param request http请求
     * @param params  {@link PlanQueryDto#getKey()} 执行记录ID
     * @return {@link CommonResult}
     */
    @PostMapping("/history/scenario/task/runAgain")
    public CommonResult reExec(HttpServletRequest request, @RequestBody PlanQueryDto params) {
        if (params.getKey() == null) {
            return CommonResult.failed("请选择正确的执行记录");
        }
        return execService.reExec(params.getKey(), parseUserName(request));
    }

    /**
     * 人工确认某条执行记录是否成功
     *
     * @param request http请求
     * @param params  {@link PlanQueryDto#getKey()} 执行记录ID {@link PlanQueryDto#getConfirm()}} 确认结果
     * @return {@link CommonResult}
     */
    @PostMapping("/history/scenario/task/ensure")
    public CommonResult success(HttpServletRequest request, @RequestBody PlanQueryDto params) {
        if (params.getKey() == null) {
            return CommonResult.failed("请选择正确的执行记录");
        }
        if ("成功".equals(params.getConfirm())) {
            return execService.ensure(params.getKey(), "5", parseUserName(request));
        }
        if ("失败".equals(params.getConfirm())) {
            return execService.ensure(params.getKey(), "6", parseUserName(request));
        }
        return CommonResult.failed("请选择确认成功或者失败");
    }


    /**
     * 查询预案的执行记录
     *
     * @param planName 预案名称
     * @param pageSize 分页大小
     * @param current  页码
     * @param sorter   排序字段
     * @param order    排序方式
     * @return {@link CommonResult}
     */
    @GetMapping("/history")
    public CommonResult allPlanExecRecords(@RequestParam(value = "keywords", required = false) String planName,
                                           @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                           @RequestParam(value = "current", defaultValue = "1") int current,
                                           @RequestParam(value = "sorter", defaultValue = "execute_time") String sorter,
                                           @RequestParam(value = "order", defaultValue = "DESC") String order) {
        CommonPage<EmergencyPlan> params = new CommonPage<>();
        params.setPageSize(pageSize);
        params.setPageIndex(current);
        params.setSortField(sorter);
        params.setSortType(order);
        EmergencyPlan plan = new EmergencyPlan();
        plan.setPlanName(planName);
        params.setObject(plan);
        return planService.allPlanExecRecords(params);
    }


    /**
     * 查询某条预案执行记录下的场景执行记录
     *
     * @param execId 执行ID
     * @return {@link CommonResult}
     */
    @GetMapping("/history/scenario")
    public CommonResult allSceneExecRecords(@RequestParam("history_id") int execId) {
        CommonPage<EmergencyExecRecord> params = new CommonPage<>();
        EmergencyExecRecord record = new EmergencyExecRecord();
        record.setExecId(execId);
        params.setObject(record);
        return planService.allSceneExecRecords(params);
    }

    /**
     * 查询某条场景执行明细下的任务执行明细
     *
     * @param execId  执行ID
     * @param sceneId 场景ID
     * @return {@link CommonResult}
     */
    @GetMapping("/history/scenario/task")
    public CommonResult allTaskExecRecords(@RequestParam("history_id") int execId,
                                           @RequestParam("scena_id") int sceneId) {
        CommonPage<EmergencyExecRecord> params = new CommonPage<>();
        EmergencyExecRecord record = new EmergencyExecRecord();
        record.setExecId(execId);
        record.setSceneId(sceneId);
        params.setObject(record);
        return planService.allTaskExecRecords(params);
    }

    /**
     * 查询某条执行记录的日志
     *
     * @param recordId 记录ID
     * @param lineNum  日志行号
     * @return {@link LogResponse}
     */
    @GetMapping("/history/scenario/task/log")
    public LogResponse getLog(@RequestParam("key") int recordId,
                              @RequestParam(value = "line", defaultValue = "1") int lineNum) {
        int lineIndex = lineNum;
        if (lineIndex <= 0) {
            lineIndex = 1;
        }
        return execService.getLog(recordId, lineIndex);
    }

    /**
     * 执行记录下载
     *
     * @param response 请求响应
     */
    @GetMapping("/download")
    public void download(HttpServletResponse response) {
        ExcelWriter excelWriter = null;
        try {
            final ServletOutputStream outputStream = response.getOutputStream();
            String fileName = "exec_records.xlsx";
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
            response.setHeader("Content-Disposition",
                "attachment;fileName=" + new String(URLEncoder.encode(fileName, "UTF-8").getBytes("UTF-8")));
            excelWriter = ExcelUtil.getBigWriter();
            excelWriter.renameSheet("执行记录汇总").write(execMapper.allRecords());
            excelWriter.flush(outputStream);
        } catch (IOException e) {
            LOGGER.error("download error.", e);
        } finally {
            if (excelWriter != null) {
                excelWriter.close();
            }
        }
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
