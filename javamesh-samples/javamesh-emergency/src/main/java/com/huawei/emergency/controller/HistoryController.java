/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.controller;

import com.huawei.common.api.CommonResult;
import com.huawei.emergency.dto.ListDetailsDto;
import com.huawei.emergency.dto.ListHistoryParam;
import com.huawei.emergency.service.HistoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

/**
 * 任务执行controller
 *
 * @since 2021-10-30
 */
@RestController
@RequestMapping("/api")
public class HistoryController {
    @Value("${user_name}")
    private String userName;

    @Autowired
    private HistoryService historyService;

    //@GetMapping("/history")
    public CommonResult listHistory(@RequestParam(value = "keywords", required = false) String keywords,
                                    @RequestParam(value = "scene_name[]", required = false) String[] sceneName,
                                    @RequestParam(value = "scene_user[]", required = false) String[] sceneUser,
                                    @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                    @RequestParam(value = "current", defaultValue = "1") int current,
                                    @RequestParam(value = "sorter", defaultValue = "execute_time") String sorter,
                                    @RequestParam(value = "order", defaultValue = "DESC") String order) {
        ListHistoryParam listHistoryParam = new ListHistoryParam(
            keywords, sceneName, sceneUser, userName, pageSize, current, sorter, order);
        return historyService.listHistory(listHistoryParam);
    }

    @GetMapping("/history/detail")
    public CommonResult listDetails(@RequestParam(value = "history_id") int historyId) {
        List<ListDetailsDto> details = historyService.listDetails(historyId);
        return CommonResult.success(details);
    }

    @GetMapping("/history/log")
    public void downloadLog(@RequestParam(value = "task_id") int id, HttpServletResponse response) {
        historyService.downloadLog(id, response);
    }
}
