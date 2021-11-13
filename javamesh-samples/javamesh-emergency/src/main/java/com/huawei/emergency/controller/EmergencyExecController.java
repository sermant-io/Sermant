/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.controller;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.huawei.common.api.CommonPage;
import com.huawei.common.api.CommonResult;
import com.huawei.emergency.dto.PlanQueryDto;
import com.huawei.emergency.entity.EmergencyExecRecord;
import com.huawei.emergency.entity.EmergencyPlan;
import com.huawei.emergency.entity.User;
import com.huawei.emergency.mapper.EmergencyExecMapper;
import com.huawei.emergency.service.EmergencyExecService;
import com.huawei.emergency.service.EmergencyPlanService;
import com.huawei.emergency.service.EmergencyTaskDetailService;
import com.huawei.script.exec.log.LogRespone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
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
    private EmergencyTaskDetailService taskDetailService;

    @Autowired
    private EmergencyExecMapper execMapper;

    @PostMapping("/history/scenario/task/runAgain")
    public CommonResult reExec(HttpServletRequest request, @RequestBody PlanQueryDto params) {
        String userName = "";
        try {
            User user = (User) request.getSession().getAttribute("userInfo");
            if (user != null) {
                userName = user.getUserName();
            }
        } catch (Exception e) {
            LOGGER.error("get user info error.", e);
        }
        if (params.getKey() == null) {
            return CommonResult.failed("请选择正确的执行记录");
        }
        return planService.reExec(params.getKey(), userName);
    }

    @PostMapping("/history/scenario/task/ensure")
    public CommonResult success(HttpServletRequest request, @RequestBody PlanQueryDto params) {
        String userName = "";
        try {
            User user = (User) request.getSession().getAttribute("userInfo");
            if (user != null) {
                userName = user.getUserName();
            }
        } catch (Exception e) {
            LOGGER.error("get user info error.", e);
        }
        if (params.getKey() == null) {
            return CommonResult.failed("请选择正确的执行记录");
        }
        if ("成功".equals(params.getConfirm())) {
            return taskDetailService.ensure(params.getKey(), "5", userName);
        }
        if ("失败".equals(params.getConfirm())) {
            return taskDetailService.ensure(params.getKey(), "6", userName);
        }
        return CommonResult.failed("请选择确认成功或者失败");
    }


    /**
     * 预案的执行记录
     *
     * @return
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
     * 查询某条预案执行记录下的场景执行明细
     *
     * @return
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
     * @return
     */
    @GetMapping("/history/scenario/task")
    public CommonResult allTaskExecRecords(@RequestParam("history_id") int execId, @RequestParam("scena_id") int sceneId) {
        CommonPage<EmergencyExecRecord> params = new CommonPage<>();
        EmergencyExecRecord record = new EmergencyExecRecord();
        record.setExecId(execId);
        record.setSceneId(sceneId);
        params.setObject(record);
        return planService.allTaskExecRecords(params);
    }

    @GetMapping("/history/scenario/task/log")
    public LogRespone getLog(@RequestParam("key") int recordId, @RequestParam(value = "line", defaultValue = "1") int lineNum) {
        int lineIndex = lineNum;
        if (lineIndex <= 0) {
            lineIndex = 1;
        }
        return execService.getLog(recordId, lineIndex);
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
