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

package com.huaweicloud.visibility.common;

import com.huaweicloud.visibility.entity.BaseInfo;
import com.huaweicloud.visibility.entity.Consanguinity;
import com.huaweicloud.visibility.entity.Contract;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collect information caching
 *
 * @author zhp
 * @since 2022-12-02
 */
public class CollectorCache {
    /**
     * Contract Information
     */
    public static final Map<String, Contract> CONTRACT_MAP = new ConcurrentHashMap<>();

    /**
     * Blood relationship
     */
    public static final Map<String, Consanguinity> CONSANGUINITY_MAP = new ConcurrentHashMap<>();

    /**
     * Registration Information
     */
    public static final Map<String, BaseInfo> REGISTRY_MAP = new ConcurrentHashMap<>();

    private CollectorCache() {
    }

    /**
     * Save contract information
     *
     * @param contract Contract Information
     */
    public static void saveContractInfo(Contract contract) {
        if (!CONTRACT_MAP.containsKey(contract.getServiceKey())) {
            CONTRACT_MAP.putIfAbsent(contract.getServiceKey(), contract);
        } else if (Objects.equals(contract.getServiceType(), ServiceType.DUBBO.getType())) {
            Contract oldContract = CONTRACT_MAP.get(contract.getServiceKey());
            oldContract.setMethodInfoList(contract.getMethodInfoList());
        } else {
            Contract oldContract = CONTRACT_MAP.get(contract.getServiceKey());
            oldContract.getMethodInfoList().addAll(contract.getMethodInfoList());
        }
    }

    /**
     * Preservation of kinship information
     *
     * @param consanguinity Consanguinity information
     */
    public static void saveConsanguinity(Consanguinity consanguinity) {
        if (CONSANGUINITY_MAP.containsKey(consanguinity.getServiceKey())) {
            Consanguinity consanguinityOld = CONSANGUINITY_MAP.get(consanguinity.getServiceKey());
            consanguinityOld.setProviders(consanguinity.getProviders());
        } else {
            CONSANGUINITY_MAP.putIfAbsent(consanguinity.getServiceKey(), consanguinity);
        }
    }
}
