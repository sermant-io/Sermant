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

package com.huawei.sermant.metricserver.service;

import com.huawei.sermant.metricserver.dto.register.AgentRegistrationDTO;
import com.huawei.sermant.metricserver.dto.register.NetworkAddressDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Agent注册信息服务
 */
@Service
public class AgentRegistrationService {

    /**
     * key: service@service-instance
     */
    Map<String, AgentRegistrationDTO> keyIndex = new HashMap<>();

    /**
     * key: hostname
     */
    Map<String, Set<AgentRegistrationDTO>> hostnameIndex = new HashMap<>();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();

    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    /**
     * 注册{@link AgentRegistrationDTO}
     *
     * @param registration 注册信息
     */
    public void register(AgentRegistrationDTO registration) {
        final String regKey = generateRegKey(registration.getService(), registration.getServiceInstance());
        writeLock.lock();
        try {
            AgentRegistrationDTO oldReg = keyIndex.put(regKey, registration);
            if (oldReg != null) {
                for (NetworkAddressDTO networkAddress : oldReg.getNetworkAddresses()) {
                    hostnameIndex.get(networkAddress.getHostname()).remove(oldReg);
                }
            }
            for (NetworkAddressDTO networkAddress : registration.getNetworkAddresses()) {
                hostnameIndex.computeIfAbsent(networkAddress.getHostname(), hostname -> new HashSet<>())
                    .add(registration);
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 查询指定service和serviceInstance的{@link AgentRegistrationDTO}
     *
     * @param service         服务
     * @param serviceInstance 服务实例
     * @return {@link AgentRegistrationDTO}实体
     */
    public AgentRegistrationDTO getRegistration(String service, String serviceInstance) {
        readLock.lock();
        try {
            return keyIndex.get(generateRegKey(service, serviceInstance));
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 查询指定hostname的{@link AgentRegistrationDTO}
     *
     * @param hostname 主机名
     * @return {@link AgentRegistrationDTO}实体
     */
    public List<AgentRegistrationDTO> getRegistrationsByHostname(String hostname) {
        readLock.lock();
        try {
            return Collections.unmodifiableList(new ArrayList<>(hostnameIndex.get(hostname)));
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 获取所有已注册的{@link AgentRegistrationDTO}实体列表
     *
     * @return {@link AgentRegistrationDTO}实体列表
     */
    public List<AgentRegistrationDTO> listRegistrations() {
        readLock.lock();
        try {
            return Collections.unmodifiableList(new ArrayList<>(keyIndex.values()));
        } finally {
            readLock.unlock();
        }
    }

    private String generateRegKey(String service, String serviceInstance) {
        return service + "@" + serviceInstance;
    }
}
