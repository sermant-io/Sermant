/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.controller;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.huawei.common.api.CommonPage;
import com.huawei.common.api.CommonResult;
import com.huawei.emergency.entity.EmergencyExecRecord;
import com.huawei.emergency.entity.EmergencyPlan;
import com.huawei.emergency.mapper.EmergencyExecMapper;
import com.huawei.emergency.service.EmergencyPlanService;
import com.huawei.emergency.service.EmergencyTaskDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletOutputStream;
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

    @Autowired
    private EmergencyPlanService planService;

    @Autowired
    private EmergencyTaskDetailService taskDetailService;

    @Autowired
    private EmergencyExecMapper execMapper;

    @PostMapping("/records")
    public CommonResult records(@RequestBody EmergencyExecRecord emergencyExecRecord){
        return CommonResult.success();
    }

    @PostMapping("/ensure")
    public CommonResult ensure(@RequestBody EmergencyExecRecord emergencyExecRecord){
        return CommonResult.success();
    }

    @PostMapping("/again")
    public CommonResult reExec(@RequestBody EmergencyExecRecord emergencyExecRecord){
        return CommonResult.success();
    }

    @PostMapping("/reExec/{recordId}")
    public CommonResult reExec(@PathVariable("recordId") int recordId) {
        return planService.reExec(recordId);
    }

    @PostMapping("/ensure/success/{recordId}")
    public CommonResult success(@PathVariable int recordId) {
        return taskDetailService.ensure(recordId,"5");
    }

    @PostMapping("/ensure/fail/{recordId}")
    public CommonResult fail(@PathVariable int recordId) {
        return taskDetailService.ensure(recordId,"6");
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
