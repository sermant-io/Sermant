/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.implement.service.xds.loadbalance;

import io.sermant.core.service.xds.XdsLoadBalanceService;
import io.sermant.core.service.xds.entity.XdsLbPolicy;
import io.sermant.implement.service.xds.cache.XdsDataCache;
import io.sermant.implement.service.xds.utils.XdsCommonUtils;

import java.util.Optional;

/**
 * XdsLoadBalanceService impl
 *
 * @author daizhenyu
 * @since 2024-08-22
 **/
public class XdsLoadBalanceServiceImpl implements XdsLoadBalanceService {
    /**
     * constructor
     */
    public XdsLoadBalanceServiceImpl() {
    }

    @Override
    public XdsLbPolicy getLbPolicyOfCluster(String clusterName) {
        Optional<String> serviceName = XdsCommonUtils.getServiceNameFromCluster(clusterName);
        if (serviceName.isPresent()) {
            return XdsDataCache.getLbPolicyOfCluster(serviceName.get(), clusterName);
        }
        return XdsLbPolicy.UNRECOGNIZED;
    }

    @Override
    public XdsLbPolicy getBaseLbPolicyOfService(String serviceName) {
        return XdsDataCache.getBaseLbPolicyOfService(serviceName);
    }
}
