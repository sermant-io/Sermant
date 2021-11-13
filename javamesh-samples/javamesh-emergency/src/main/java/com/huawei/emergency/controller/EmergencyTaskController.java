/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.controller;

import com.huawei.common.api.CommonResult;
import com.huawei.emergency.entity.EmergencyTask;
import com.huawei.emergency.service.EmergencyTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 任务管理controller
 *
 * @author y30010171
 * @since 2021-11-04
 **/
@RestController
@RequestMapping("/task")
public class EmergencyTaskController {

    @Autowired
    private EmergencyTaskService taskService;

    @PostMapping("/bind")
    public CommonResult bind(@RequestBody EmergencyTask task) {
        return taskService.bind(task);
    }

    @DeleteMapping("/bind")
    public CommonResult unBind(@RequestBody EmergencyTask task) {
        return taskService.unBind(task);
    }

    @PostMapping("/add")
    public CommonResult add(@RequestBody EmergencyTask task){
        return taskService.add(task);
    }
}
