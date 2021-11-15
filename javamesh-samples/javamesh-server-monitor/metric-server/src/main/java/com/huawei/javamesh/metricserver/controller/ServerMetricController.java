/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.controller;

import com.huawei.javamesh.metricserver.dto.servermonitor.CpuDTO;
import com.huawei.javamesh.metricserver.dto.servermonitor.DiskDTO;
import com.huawei.javamesh.metricserver.dto.servermonitor.MemoryDTO;
import com.huawei.javamesh.metricserver.dto.servermonitor.NetworkDTO;
import com.huawei.javamesh.metricserver.service.ServerMetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Server monitor metric controller
 */
@RestController
@RequestMapping("/server-metrics")
public class ServerMetricController {

    private final ServerMetricService serverMetricService;

    @Autowired
    public ServerMetricController(ServerMetricService serverMetricService) {
        this.serverMetricService = serverMetricService;
    }

    @GetMapping("/cpu")
    public List<CpuDTO> getCpuMetrics(@RequestParam String start,
                                      @RequestParam(required = false) String end) {
        return serverMetricService.getCpuMetrics(start, end);
    }

    @GetMapping("/memory")
    public List<MemoryDTO> getMemoryMetrics(@RequestParam String start,
                                            @RequestParam(required = false) String end) {
        return serverMetricService.getMemoryMetrics(start, end);
    }

    @GetMapping("/network")
    public List<NetworkDTO> getNetworkMetrics(@RequestParam String start,
                                              @RequestParam(required = false) String end) {
        return serverMetricService.getNetworkMetrics(start, end);
    }

    @GetMapping("/disk/read-rate")
    public List<DiskDTO> getDiskReadRateMetrics(@RequestParam String start,
                                                @RequestParam(required = false) String end) {
        return serverMetricService.getDiskMetrics(DiskDTO.ValueType.READ_RATE, start, end);
    }

    @GetMapping("/disk/write-rate")
    public List<DiskDTO> getDiskWriteRateMetrics(@RequestParam String start,
                                                 @RequestParam(required = false) String end) {
        return serverMetricService.getDiskMetrics(DiskDTO.ValueType.WRITE_RATE, start, end);
    }

    @GetMapping("/disk/io-spent-percentage")
    public List<DiskDTO> getDiskIoSpentMetrics(@RequestParam String start,
                                               @RequestParam(required = false) String end) {
        return serverMetricService.getDiskMetrics(DiskDTO.ValueType.IO_SPENT_PERCENTAGE, start, end);
    }
}
