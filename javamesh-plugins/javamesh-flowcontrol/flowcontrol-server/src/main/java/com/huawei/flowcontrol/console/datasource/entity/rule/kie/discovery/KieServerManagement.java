/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

import com.huawei.flowcontrol.console.datasource.entity.rule.kie.discovery.common.KieServerInfo;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.discovery.common.KieServerLabel;
import com.huawei.flowcontrol.console.entity.MachineInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * kie服务器管理侧，负责kie服务器增删查等操作
 *
 * @author Sherlockhan
 * @since 2020-12-21
 */
@Component
public class KieServerManagement implements KieServerDiscovery {
    private static final int NEW_KIE_MACHINE_NUM = 1;

    @Autowired
    @Qualifier("SimpleKieDiscovery")
    KieServerDiscovery discovery;

    @PostConstruct
    public void init() {
    }

    @Override
    public long addMachineInfo(KieServerLabel label, MachineInfo machineInfo) {
        discovery.addMachineInfo(label, machineInfo);
        return NEW_KIE_MACHINE_NUM;
    }

    @Override
    public boolean removeMachineInfo(String project, String app, String server, String environment, String version) {
        return false;
    }

    @Override
    public Optional<KieServerInfo> queryKieInfo(String id) {
        return discovery.queryKieInfo(id);
    }

    @Override
    public List<String> getServerIds() {
        return discovery.getServerIds();
    }

    @Override
    public void removeServer(String id) {
        discovery.removeServer(id);
    }

    @Override
    public Set<MachineInfo> getMachineInfos(String id) {
        return discovery.getMachineInfos(id);
    }
}
