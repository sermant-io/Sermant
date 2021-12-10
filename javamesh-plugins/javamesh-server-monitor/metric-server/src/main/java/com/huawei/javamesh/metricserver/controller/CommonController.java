/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
