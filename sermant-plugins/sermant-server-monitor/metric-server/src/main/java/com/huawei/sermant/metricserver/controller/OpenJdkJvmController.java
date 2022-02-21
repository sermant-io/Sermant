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

package com.huawei.sermant.metricserver.controller;

import com.huawei.sermant.metricserver.dto.openjdk.CpuDTO;
import com.huawei.sermant.metricserver.dto.openjdk.GcDTO;
import com.huawei.sermant.metricserver.dto.openjdk.OpenJdkMemoryDTO;
import com.huawei.sermant.metricserver.dto.openjdk.OpenJdkMemoryPoolDTO;
import com.huawei.sermant.metricserver.dto.openjdk.ThreadDTO;
import com.huawei.sermant.metricserver.service.OpenJdkMemoryPoolService;
import com.huawei.sermant.metricserver.service.OpenJdkMemoryService;
import com.huawei.sermant.metricserver.service.OpenJdkJvmMetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * OpenJdk jvm metric controller
 */
@RestController
@RequestMapping("/open-jdk-jvm-metrics")
public class OpenJdkJvmController {

    private final OpenJdkMemoryService memoryService;

    private final OpenJdkMemoryPoolService memoryPoolService;

    private final OpenJdkJvmMetricService jvmMetricService;

    @Autowired
    public OpenJdkJvmController(OpenJdkMemoryService memoryService,
                                OpenJdkMemoryPoolService memoryPoolService,
                                OpenJdkJvmMetricService jvmMetricService) {
        this.memoryService = memoryService;
        this.memoryPoolService = memoryPoolService;
        this.jvmMetricService = jvmMetricService;
    }

    @GetMapping("/cpu")
    public List<CpuDTO> getCpuMetrics(
            @RequestParam String start,
            @RequestParam(required = false) String end) {
        return jvmMetricService.getCpuMetrics(start, end);
    }

    @GetMapping("/thread")
    public List<ThreadDTO> getThreadMetrics(
            @RequestParam String start,
            @RequestParam(required = false) String end) {
        return jvmMetricService.getThreadMetrics(start, end);
    }

    @GetMapping("/gc/young")
    public List<GcDTO> getYoungGcMetrics(
            @RequestParam String start,
            @RequestParam(required = false) String end) {
        return jvmMetricService.getGcMetrics(start, end, GcDTO.GcType.YOUNG);
    }

    @GetMapping("/gc/old")
    public List<GcDTO> getOldGcMetrics(
            @RequestParam String start,
            @RequestParam(required = false) String end) {
        return jvmMetricService.getGcMetrics(start, end, GcDTO.GcType.OLD);
    }

    @GetMapping("/memory/heap")
    public List<OpenJdkMemoryDTO> getHeapMemoryMetrics(
            @RequestParam String start,
            @RequestParam(required = false) String end) {
        return memoryService.getMemoryMetrics(OpenJdkMemoryDTO.OracleMemoryType.HEAP, start, end);
    }

    @GetMapping("/memory/non-heap")
    public List<OpenJdkMemoryDTO> getNonHeapMemoryMetrics(
            @RequestParam String start,
            @RequestParam(required = false) String end) {
        return memoryService.getMemoryMetrics(OpenJdkMemoryDTO.OracleMemoryType.NON_HEAP, start, end);
    }

    @GetMapping("/memory-pool/code-cache")
    public List<OpenJdkMemoryPoolDTO> getCodeCachePoolMetrics(
            @RequestParam String start,
            @RequestParam(required = false) String end) {
        return memoryPoolService.getMemoryPools(OpenJdkMemoryPoolDTO.OraclePoolType.CODE_CACHE, start, end);
    }

    @GetMapping("/memory-pool/new-gen")
    public List<OpenJdkMemoryPoolDTO> getNewGenPoolMetrics(
            @RequestParam String start,
            @RequestParam(required = false) String end) {
        return memoryPoolService.getMemoryPools(OpenJdkMemoryPoolDTO.OraclePoolType.NEW_GEN, start, end);
    }

    @GetMapping("/memory-pool/old-gen")
    public List<OpenJdkMemoryPoolDTO> getOldGenPoolMetrics(
            @RequestParam String start,
            @RequestParam(required = false) String end) {
        return memoryPoolService.getMemoryPools(OpenJdkMemoryPoolDTO.OraclePoolType.OLD_GEN, start, end);
    }

    @GetMapping("/memory-pool/survivor")
    public List<OpenJdkMemoryPoolDTO> getSurvivorPoolMetrics(
            @RequestParam String start,
            @RequestParam(required = false) String end) {
        return memoryPoolService.getMemoryPools(OpenJdkMemoryPoolDTO.OraclePoolType.SURVIVOR, start, end);
    }

    @GetMapping("/memory-pool/perm-gen")
    public List<OpenJdkMemoryPoolDTO> getPermGenPoolMetrics(
            @RequestParam String start,
            @RequestParam(required = false) String end) {
        return memoryPoolService.getMemoryPools(OpenJdkMemoryPoolDTO.OraclePoolType.PERM_GEN, start, end);
    }

    @GetMapping("/memory-pool/metaspace")
    public List<OpenJdkMemoryPoolDTO> getMetaspacePoolMetrics(
            @RequestParam String start,
            @RequestParam(required = false) String end) {
        return memoryPoolService.getMemoryPools(OpenJdkMemoryPoolDTO.OraclePoolType.METASPACE, start, end);
    }
}
