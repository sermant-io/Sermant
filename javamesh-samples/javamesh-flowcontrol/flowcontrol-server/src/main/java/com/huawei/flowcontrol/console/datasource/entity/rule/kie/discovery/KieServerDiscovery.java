/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.datasource.entity.rule.kie.discovery;

import com.huawei.flowcontrol.console.datasource.entity.rule.kie.discovery.common.KieServerInfo;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.discovery.common.KieServerLabel;
import com.huawei.flowcontrol.console.entity.MachineInfo;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * kie服务发现的接口层
 *
 * @author Sherlockhan
 * @since 2020-12-21
 */
public interface KieServerDiscovery {
    long addMachineInfo(KieServerLabel label, MachineInfo machineInfo);

    boolean removeMachineInfo(String project, String app, String server, String environment, String version);

    Optional<KieServerInfo> queryKieInfo(String id);

    List<String> getServerIds();

    void removeServer(String id);

    Set<MachineInfo> getMachineInfos(String id);
}
