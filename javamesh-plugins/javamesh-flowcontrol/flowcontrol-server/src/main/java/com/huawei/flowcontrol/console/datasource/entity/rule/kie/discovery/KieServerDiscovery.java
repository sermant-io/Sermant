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
