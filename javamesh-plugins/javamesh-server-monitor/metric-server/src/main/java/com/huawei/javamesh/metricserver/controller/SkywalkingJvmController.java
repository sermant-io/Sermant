/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.controller;

import com.huawei.javamesh.metricserver.dto.skywalkingjvm.CpuDTO;
import com.huawei.javamesh.metricserver.dto.skywalkingjvm.GcDTO;
import com.huawei.javamesh.metricserver.dto.skywalkingjvm.OracleMemoryDTO;
import com.huawei.javamesh.metricserver.dto.skywalkingjvm.OracleMemoryPoolDTO;
import com.huawei.javamesh.metricserver.dto.skywalkingjvm.ThreadDTO;
import com.huawei.javamesh.metricserver.service.OracleMemoryPoolService;
import com.huawei.javamesh.metricserver.service.OracleMemoryService;
import com.huawei.javamesh.metricserver.service.SkywalkingJvmMetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Skywalking jvm metric controller
 */
@RestController
@RequestMapping("/oracle-jvm-metrics")
public class SkywalkingJvmController {

    private final OracleMemoryService memoryService;

    private final OracleMemoryPoolService memoryPoolService;

    private final SkywalkingJvmMetricService jvmMetricService;

    @Autowired
    public SkywalkingJvmController(OracleMemoryService memoryService,
                                   OracleMemoryPoolService memoryPoolService,
                                   SkywalkingJvmMetricService jvmMetricService) {
        this.memoryService = memoryService;
        this.memoryPoolService = memoryPoolService;
        this.jvmMetricService = jvmMetricService;
    }

    @GetMapping("/cpu")
    public List<CpuDTO> getCpuMetrics(@RequestParam String start,
                                      @RequestParam(required = false) String end) {
        return jvmMetricService.getCpuMetrics(start, end);
    }

    @GetMapping("/thread")
    public List<ThreadDTO> getThreadMetrics(@RequestParam String start,
                                            @RequestParam(required = false) String end) {
        return jvmMetricService.getThreadMetrics(start, end);
    }

    @GetMapping("/gc/young")
    public List<GcDTO> getYoungGcMetrics(@RequestParam String start,
                                         @RequestParam(required = false) String end) {
        return jvmMetricService.getGcMetrics(start, end, GcDTO.GcType.YOUNG);
    }

    @GetMapping("/gc/old")
    public List<GcDTO> getOldGcMetrics(@RequestParam String start,
                                       @RequestParam(required = false) String end) {
        return jvmMetricService.getGcMetrics(start, end, GcDTO.GcType.OLD);
    }

    @GetMapping("/memory/heap")
    public List<OracleMemoryDTO> getHeapMemoryMetrics(@RequestParam String start,
                                                      @RequestParam(required = false) String end) {
        return memoryService.getMemoryMetrics(OracleMemoryDTO.OracleMemoryType.HEAP, start, end);
    }

    @GetMapping("/memory/non-heap")
    public List<OracleMemoryDTO> getNonHeapMemoryMetrics(@RequestParam String start,
                                                         @RequestParam(required = false) String end) {
        return memoryService.getMemoryMetrics(OracleMemoryDTO.OracleMemoryType.NON_HEAP, start, end);
    }

    @GetMapping("/memory-pool/code-cache")
    public List<OracleMemoryPoolDTO> getCodeCachePoolMetrics(@RequestParam String start,
                                                             @RequestParam(required = false) String end) {
        return memoryPoolService.getMemoryPools(OracleMemoryPoolDTO.OraclePoolType.CODE_CACHE, start, end);
    }

    @GetMapping("/memory-pool/new-gen")
    public List<OracleMemoryPoolDTO> getNewGenPoolMetrics(@RequestParam String start,
                                                          @RequestParam(required = false) String end) {
        return memoryPoolService.getMemoryPools(OracleMemoryPoolDTO.OraclePoolType.NEW_GEN, start, end);
    }

    @GetMapping("/memory-pool/old-gen")
    public List<OracleMemoryPoolDTO> getOldGenPoolMetrics(@RequestParam String start,
                                                          @RequestParam(required = false) String end) {
        return memoryPoolService.getMemoryPools(OracleMemoryPoolDTO.OraclePoolType.OLD_GEN, start, end);
    }

    @GetMapping("/memory-pool/survivor")
    public List<OracleMemoryPoolDTO> getSurvivorPoolMetrics(@RequestParam String start,
                                                            @RequestParam(required = false) String end) {
        return memoryPoolService.getMemoryPools(OracleMemoryPoolDTO.OraclePoolType.SURVIVOR, start, end);
    }

    @GetMapping("/memory-pool/perm-gen")
    public List<OracleMemoryPoolDTO> getPermGenPoolMetrics(@RequestParam String start,
                                                           @RequestParam(required = false) String end) {
        return memoryPoolService.getMemoryPools(OracleMemoryPoolDTO.OraclePoolType.PERM_GEN, start, end);
    }

    @GetMapping("/memory-pool/metaspace")
    public List<OracleMemoryPoolDTO> getMetaspacePoolMetrics(@RequestParam String start,
                                                             @RequestParam(required = false) String end) {
        return memoryPoolService.getMemoryPools(OracleMemoryPoolDTO.OraclePoolType.METASPACE, start, end);
    }
}
