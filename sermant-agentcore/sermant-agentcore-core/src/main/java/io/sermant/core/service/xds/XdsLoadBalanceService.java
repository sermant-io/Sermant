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

package io.sermant.core.service.xds;

import io.sermant.core.service.xds.entity.XdsLbPolicy;

/**
 * xDS load balance service
 *
 * @author daizhenyu
 * @since 2024-07-30
 **/
public interface XdsLoadBalanceService {
    /**
     * get lb policy of cluster
     *
     * @param clusterName cluster name
     * @return route rules
     */
    XdsLbPolicy getLbPolicyOfCluster(String clusterName);

    /**
     * get lb policy of service (base cluster)
     *
     * @param serviceName service name
     * @return route rules
     */
    XdsLbPolicy getBaseLbPolicyOfService(String serviceName);
}
