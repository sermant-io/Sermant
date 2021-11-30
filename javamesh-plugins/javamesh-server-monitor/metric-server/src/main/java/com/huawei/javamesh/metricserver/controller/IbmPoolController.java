/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.controller;

import com.huawei.javamesh.metricserver.dto.ibmpool.IbmMemoryPoolDTO;
import com.huawei.javamesh.metricserver.dto.ibmpool.IbmPoolType;
import com.huawei.javamesh.metricserver.service.IbmMemoryPoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * IBM Memory Pool Controller
 */
@RestController
@RequestMapping("/ibm-jvm-metrics")
public class IbmPoolController {

    private final IbmMemoryPoolService service;

    @Autowired
    public IbmPoolController(IbmMemoryPoolService service) {
        this.service = service;
    }

    @GetMapping("/memory-pool/jcc")
    public List<IbmMemoryPoolDTO> getJITCodeCachePoolMetrics(@RequestParam String start,
                                                             @RequestParam(required = false) String end) {
        return service.getMemoryPools(IbmPoolType.JCC, start, end);
    }

    @GetMapping("/memory-pool/jdc")
    public List<IbmMemoryPoolDTO> getJITDataCachePoolMetrics(@RequestParam String start,
                                                             @RequestParam(required = false) String end) {
        return service.getMemoryPools(IbmPoolType.JDC, start, end);
    }

    @GetMapping("/memory-pool/ts")
    public List<IbmMemoryPoolDTO> getTenuredSOAPoolMetrics(@RequestParam String start,
                                                           @RequestParam(required = false) String end) {
        return service.getMemoryPools(IbmPoolType.TS, start, end);
    }

    @GetMapping("/memory-pool/tl")
    public List<IbmMemoryPoolDTO> getTenuredLOAPoolMetrics(@RequestParam String start,
                                                           @RequestParam(required = false) String end) {
        return service.getMemoryPools(IbmPoolType.TL, start, end);
    }

    @GetMapping("/memory-pool/na")
    public List<IbmMemoryPoolDTO> getNurseryAllocatePoolMetrics(@RequestParam String start,
                                                                @RequestParam(required = false) String end) {
        return service.getMemoryPools(IbmPoolType.NA, start, end);
    }

    @GetMapping("/memory-pool/ns")
    public List<IbmMemoryPoolDTO> getNurserySurvivorPoolMetrics(@RequestParam String start,
                                                                @RequestParam(required = false) String end) {
        return service.getMemoryPools(IbmPoolType.NS, start, end);
    }

    @GetMapping("/memory-pool/cs")
    public List<IbmMemoryPoolDTO> getClassStoragePoolMetrics(@RequestParam String start,
                                                             @RequestParam(required = false) String end) {
        return service.getMemoryPools(IbmPoolType.CS, start, end);
    }

    @GetMapping("/memory-pool/mnhs")
    public List<IbmMemoryPoolDTO> getMiscellaneousNonHeapStoragePoolMetrics(@RequestParam String start,
                                                                            @RequestParam(required = false) String end) {
        return service.getMemoryPools(IbmPoolType.MNHS, start, end);
    }
}
