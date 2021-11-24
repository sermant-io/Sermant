/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.controller.monitor;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huawei.hercules.controller.monitor.dto.AgentRegistrationDTO;
import com.huawei.hercules.controller.monitor.dto.NetworkAddressDTO;
import com.huawei.hercules.controller.perftest.MonitorHostKey;
import com.huawei.hercules.controller.perftest.TaskInfoKey;
import com.huawei.hercules.exception.HerculesException;
import com.huawei.hercules.service.influxdb.IHostApplicationMapping;
import com.huawei.hercules.service.influxdb.IMonitorService;
import com.huawei.hercules.controller.monitor.dto.MonitorHostDTO;
import com.huawei.hercules.service.influxdb.metric.tree.impl.RootMetricNode;
import com.huawei.hercules.service.perftest.IPerfTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 功能描述：监控分析数据获取
 *
 * @author z30009938
 * @since 2021-11-12
 */
@RestController
@RequestMapping("/api")
public class MonitorController {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorController.class);

    @Autowired
    private IMonitorService monitorService;

    @Autowired
    private IPerfTestService perfTestService;

    @Autowired
    private IHostApplicationMapping hostApplicationMapping;

    @GetMapping("/monitor")
    public MonitorModel getMonitorInfo(@RequestParam MonitorHostDTO monitorHostDTO) {
        LOGGER.debug("Monitor param:{}", monitorHostDTO);
        MonitorModel monitorModel = new MonitorModel();
        JSONObject perfTestInfo = perfTestService.getOne(monitorHostDTO.getTestId());
        JSONArray monitorHosts = perfTestInfo.getJSONArray(TaskInfoKey.MONITORING_HOST.getServerKey());
        initJvmConfig(monitorHostDTO, monitorHosts);
        initMonitoredServiceConfig(monitorHostDTO);
        RootMetricNode allMonitorData = monitorService.getAllMonitorData(monitorHostDTO);
        monitorModel.setSuccess(true);
        monitorModel.setData(allMonitorData);
        return monitorModel;
    }

    /**
     * 查询需要监控的主机是否需要获取jvm数据
     *
     * @param monitorHostDTO     查询传入的参数信息
     * @param monitorHosts 系统查询到的主机信息
     */
    private void initJvmConfig(MonitorHostDTO monitorHostDTO, JSONArray monitorHosts) {
        if (monitorHostDTO == null || StringUtils.isEmpty(monitorHostDTO.getHost()) || StringUtils.isEmpty(monitorHostDTO.getIp())) {
            throw new HerculesException("Host or ip can not be empty for monitoring.");
        }
        if (monitorHosts == null || monitorHosts.isEmpty()) {
            throw new HerculesException("Can not found this host in config of this test.");
        }
        for (int i = 0; i < monitorHosts.size(); i++) {
            JSONObject monitorHost = monitorHosts.getJSONObject(i);
            if (!monitorHostDTO.getHost().equals(monitorHost.getString(MonitorHostKey.HOST.getServerKey()))) {
                continue;
            }
            if (!monitorHostDTO.getIp().equals(monitorHost.getString(MonitorHostKey.IP.getServerKey()))) {
                continue;
            }
            Boolean isMonitorJvm = monitorHost.getBoolean(MonitorHostKey.IS_MONITOR_JVM.getServerKey());
            String jvmType = monitorHost.getString(MonitorHostKey.JVM_TYPE.getServerKey());
            monitorHostDTO.setJvmType(jvmType);
            monitorHostDTO.setMonitorJvm(isMonitorJvm);
            break;
        }
    }

    /**
     * 初始化传入机器信息的服务信息，后面需要用这些信息去influxDB查询数据
     *
     * @param monitorHostDTO 保存参数的中间变量
     */
    private void initMonitoredServiceConfig(MonitorHostDTO monitorHostDTO) {
        if (monitorHostDTO == null) {
            throw new HerculesException("Param can not be null.");
        }
        String host = monitorHostDTO.getHost();
        String ip = monitorHostDTO.getIp();
        if (StringUtils.isEmpty(host) || StringUtils.isEmpty(ip)) {
            throw new HerculesException("Host or ip can not be empty for monitoring.");
        }
        List<AgentRegistrationDTO> registrations = hostApplicationMapping.getRegistrationsByHostname(host);
        if (registrations == null || registrations.isEmpty()) {
            throw new HerculesException("Can not found service info from agent server.");
        }
        for (AgentRegistrationDTO registration : registrations) {
            List<NetworkAddressDTO> networkAddresses = registration.getNetworkAddresses();
            if (networkAddresses == null || networkAddresses.isEmpty()) {
                throw new HerculesException("Service information can be found, but there is no IP information.");
            }
            for (NetworkAddressDTO networkAddress : networkAddresses) {
                if (host.equals(networkAddress.getHostname()) && ip.equals(networkAddress.getAddress())) {
                    monitorHostDTO.setService(registration.getService());
                    monitorHostDTO.setServiceInstance(registration.getServiceInstance());
                    break;
                }
            }
        }
    }
}
