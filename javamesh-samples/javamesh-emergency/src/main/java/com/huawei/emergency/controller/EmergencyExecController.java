/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.controller;

import com.huawei.common.api.CommonResult;
import com.huawei.emergency.entity.EmergencyExecRecord;
import com.huawei.emergency.service.EmergencyPlanService;
import com.huawei.emergency.service.EmergencyTaskDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 执行记录管理
 *
 * @author y30010171
 * @since 2021-11-09
 **/
@RestController
@RequestMapping("/exec")
public class EmergencyExecController {

    @Autowired
    private EmergencyPlanService planService;

    @Autowired
    EmergencyTaskDetailService taskDetailService;

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
}
