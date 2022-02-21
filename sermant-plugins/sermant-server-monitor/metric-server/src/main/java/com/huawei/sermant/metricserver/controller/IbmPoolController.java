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

import com.huawei.sermant.metricserver.dto.ibmpool.IbmMemoryPoolDTO;
import com.huawei.sermant.metricserver.dto.ibmpool.IbmPoolType;
import com.huawei.sermant.metricserver.service.IbmMemoryPoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public List<IbmMemoryPoolDTO> getJitCodeCachePoolMetrics(
            @RequestParam String start,
            @RequestParam(required = false) String end) {
        return service.getMemoryPools(IbmPoolType.JCC, start, end);
    }

    @GetMapping("/memory-pool/jdc")
    public List<IbmMemoryPoolDTO> getJitDataCachePoolMetrics(
            @RequestParam String start,
            @RequestParam(required = false) String end) {
        return service.getMemoryPools(IbmPoolType.JDC, start, end);
    }

    @GetMapping("/memory-pool/ts")
    public List<IbmMemoryPoolDTO> getTenuredSoaPoolMetrics(
            @RequestParam String start,
            @RequestParam(required = false) String end) {
        return service.getMemoryPools(IbmPoolType.TS, start, end);
    }

    @GetMapping("/memory-pool/tl")
    public List<IbmMemoryPoolDTO> getTenuredLoaPoolMetrics(
            @RequestParam String start,
            @RequestParam(required = false) String end) {
        return service.getMemoryPools(IbmPoolType.TL, start, end);
    }

    @GetMapping("/memory-pool/na")
    public List<IbmMemoryPoolDTO> getNurseryAllocatePoolMetrics(
            @RequestParam String start,
            @RequestParam(required = false) String end) {
        return service.getMemoryPools(IbmPoolType.NA, start, end);
    }

    @GetMapping("/memory-pool/ns")
    public List<IbmMemoryPoolDTO> getNurserySurvivorPoolMetrics(
            @RequestParam String start,
            @RequestParam(required = false) String end) {
        return service.getMemoryPools(IbmPoolType.NS, start, end);
    }

    @GetMapping("/memory-pool/cs")
    public List<IbmMemoryPoolDTO> getClassStoragePoolMetrics(
            @RequestParam String start,
            @RequestParam(required = false) String end) {
        return service.getMemoryPools(IbmPoolType.CS, start, end);
    }

    @GetMapping("/memory-pool/mnhs")
    public List<IbmMemoryPoolDTO> getMiscellaneousNonHeapStoragePoolMetrics(
            @RequestParam String start,
            @RequestParam(required = false) String end) {
        return service.getMemoryPools(IbmPoolType.MNHS, start, end);
    }
}
