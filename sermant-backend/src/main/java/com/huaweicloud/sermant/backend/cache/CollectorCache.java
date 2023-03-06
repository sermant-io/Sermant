/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.sermant.backend.cache;

import com.huaweicloud.sermant.backend.entity.Consanguinity;
import com.huaweicloud.sermant.backend.entity.Contract;
import com.huaweicloud.sermant.backend.entity.ServerInfo;
import com.huaweicloud.sermant.backend.entity.ServiceType;

import com.alibaba.fastjson.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 采集信息缓存
 *
 * @author zhp
 * @since 2022-12-05
 */
public class CollectorCache {
    /**
     * 服务信息
     */
    public static final Map<String, ServerInfo> SERVER_MAP = new ConcurrentHashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectorCache.class);

    private CollectorCache() {
    }

    /**
     * 保存契约信息
     *
     * @param serverInfo 服务采集信息
     */
    public static void saveInfo(ServerInfo serverInfo) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("collector server info is {}", JSONObject.toJSONString(serverInfo));
        }
        StringBuilder stringBuilder = new StringBuilder(serverInfo.getApplicationName());
        stringBuilder.append(serverInfo.getVersion()).append(serverInfo.getGroupName())
                .append(serverInfo.getEnvironment()).append(serverInfo.getZone()).append(serverInfo.getProject());
        if (SERVER_MAP.get(stringBuilder.toString()) == null) {
            List<String> instanceIds = new ArrayList<>();
            instanceIds.add(serverInfo.getInstanceId());
            serverInfo.setInstanceIds(instanceIds);
            SERVER_MAP.putIfAbsent(stringBuilder.toString(), serverInfo);
        }
        ServerInfo oldServerInfo = SERVER_MAP.get(stringBuilder.toString());
        if (oldServerInfo == serverInfo) {
            return;
        }
        if (oldServerInfo.getInstanceIds() != null
                && !oldServerInfo.getInstanceIds().contains(serverInfo.getInstanceId())) {
            oldServerInfo.getInstanceIds().add(serverInfo.getInstanceId());
        }
        if (serverInfo.getContractList() != null) {
            saveContractor(serverInfo, oldServerInfo);
        }
        if (serverInfo.getConsanguinityList() != null) {
            saveConsanguinityList(serverInfo, oldServerInfo);
        }
        if (serverInfo.getRegistryInfo() != null) {
            oldServerInfo.setRegistryInfo(serverInfo.getRegistryInfo());
        }
    }

    /**
     * 保存契约信息
     *
     * @param serverInfo    服务信息
     * @param oldServerInfo 更新前的服务信息
     */
    private static void saveContractor(ServerInfo serverInfo, ServerInfo oldServerInfo) {
        if (oldServerInfo.getContractList() == null) {
            oldServerInfo.setContractList(serverInfo.getContractList());
        } else {
            Map<String, Contract> map = new HashMap<>();
            for (Contract contract : oldServerInfo.getContractList()) {
                map.put(contract.getServiceKey(), contract);
            }
            for (Contract contract : serverInfo.getContractList()) {
                if (map.get(contract.getServiceKey()) == null) {
                    oldServerInfo.getContractList().add(contract);
                    map.put(contract.getServiceKey(), contract);
                } else if (Objects.equals(contract.getServiceType(), ServiceType.DUBBO.getType())) {
                    Contract oldContract = map.get(contract.getServiceKey());
                    oldContract.setMethodInfoList(contract.getMethodInfoList());
                } else {
                    Contract oldContract = map.get(contract.getServiceKey());
                    oldServerInfo.getContractList().remove(oldContract);
                    oldServerInfo.getContractList().add(contract);
                }
            }
        }
    }

    /**
     * 保存血缘关系信息
     *
     * @param serverInfo    服务信息
     * @param oldServerInfo 旧服务信息
     */
    private static void saveConsanguinityList(ServerInfo serverInfo, ServerInfo oldServerInfo) {
        if (oldServerInfo.getConsanguinityList() == null) {
            oldServerInfo.setConsanguinityList(serverInfo.getConsanguinityList());
        } else {
            Map<String, Consanguinity> map = new ConcurrentHashMap<>();
            for (Consanguinity consanguinity : oldServerInfo.getConsanguinityList()) {
                map.put(consanguinity.getServiceKey(), consanguinity);
            }
            for (Consanguinity consanguinity : serverInfo.getConsanguinityList()) {
                if (map.get(consanguinity.getServiceKey()) != null) {
                    map.get(consanguinity.getServiceKey()).setProviders(consanguinity.getProviders());
                } else {
                    map.put(consanguinity.getServiceKey(), consanguinity);
                    oldServerInfo.getConsanguinityList().add(consanguinity);
                }
            }
        }
    }

    /**
     * 移除服务
     *
     * @param serverInfo 服务信息
     */
    public static void removeServer(ServerInfo serverInfo) {
        LOGGER.info("need clean collector server info is {}", JSONObject.toJSONString(serverInfo));
        ServerInfo oldServerInfo = null;
        for (ServerInfo info : SERVER_MAP.values()) {
            if (info != null && info.getInstanceIds() != null
                    && info.getInstanceIds().contains(serverInfo.getInstanceId())) {
                oldServerInfo = info;
            }
        }
        if (oldServerInfo == null) {
            return;
        }
        if (oldServerInfo.getInstanceIds().size() <= 1
                && oldServerInfo.getInstanceIds().contains(serverInfo.getInstanceId())
                && SERVER_MAP.containsValue(oldServerInfo)) {
            SERVER_MAP.values().remove(oldServerInfo);
        } else {
            oldServerInfo.getInstanceIds().remove(serverInfo.getInstanceId());
        }
    }
}
