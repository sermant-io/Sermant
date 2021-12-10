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

package com.huawei.flowcontrol.console.datasource.entity.rule.kie.discovery;

import com.alibaba.csp.sentinel.util.AssertUtil;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.discovery.common.KieServerInfo;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.discovery.common.KieServerLabel;
import com.huawei.flowcontrol.console.entity.AppInfo;
import com.huawei.flowcontrol.console.entity.MachineInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 用来做kie服务发现，查找kie服务器.
 *
 * @author Sherlockhan
 * @since 2020-12-21
 */
@Component(value = "SimpleKieDiscovery")
public class SimpleKieDiscovery implements KieServerDiscovery {
    private static final int NEW_KIE_MACHINE_NUM = 1;

    ConcurrentHashMap<KieServerLabel, KieServerInfo> serverMap = new ConcurrentHashMap<>();

    @Override
    public long addMachineInfo(KieServerLabel labelInfo, MachineInfo machineInfo) {
        AssertUtil.notNull(labelInfo, "labelInfo cannot be null");

        KieServerInfo kieServerInfo = serverMap.computeIfAbsent(labelInfo,
            label -> new KieServerInfo(UUID.randomUUID().toString(), labelInfo));

        kieServerInfo.addMachine(machineInfo);
        return NEW_KIE_MACHINE_NUM;
    }

    @Override
    public boolean removeMachineInfo(String project, String app, String server, String environment, String version) {
        return false;
    }

    @Override
    public Optional<KieServerInfo> queryKieInfo(String id) {
        return serverMap.values().stream()
            .filter(value -> value.getId().equals(id))
            .findFirst();
    }

    @Override
    public List<String> getServerIds() {
        return serverMap.values().stream().map(KieServerInfo::getId)
            .collect(Collectors.toList());
    }

    @Override
    public void removeServer(String id) {
        serverMap.entrySet().removeIf(entry -> entry.getValue().getId().equals(id));
    }

    @Override
    public Set<MachineInfo> getMachineInfos(String serverId) {
        Optional<KieServerInfo> kieServerInfo = queryKieInfo(serverId);
        return kieServerInfo.map(AppInfo::getMachines).orElse(null);
    }
}
