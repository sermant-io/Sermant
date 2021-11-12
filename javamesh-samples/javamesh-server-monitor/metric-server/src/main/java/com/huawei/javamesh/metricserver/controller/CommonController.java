/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.controller;

import com.huawei.javamesh.metricserver.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Common Controller
 */
@RestController
@RequestMapping("/metrics")
public class CommonController {

    private final CommonService service;

    @Autowired
    public CommonController(CommonService service) {
        this.service = service;
    }

    /**
     * 删除过去7天的数据（谨慎使用）
     */
    @GetMapping("/delete/7d")
    public void deleteMetricOfPast7Days() {
        final OffsetDateTime now = OffsetDateTime.now();
        service.delete(now.minus(7, ChronoUnit.DAYS), now);
    }

    /**
     * 删除过去1天的数据（谨慎使用）
     */
    @GetMapping("/delete/1d")
    public void deleteMetricOfPastDay() {
        final OffsetDateTime now = OffsetDateTime.now();
        service.delete(now.minus(1, ChronoUnit.DAYS), now);
    }
}
