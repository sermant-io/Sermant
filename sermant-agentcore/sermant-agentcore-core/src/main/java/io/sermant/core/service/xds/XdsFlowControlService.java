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

import io.sermant.core.service.xds.entity.XdsHttpFault;
import io.sermant.core.service.xds.entity.XdsInstanceCircuitBreakers;
import io.sermant.core.service.xds.entity.XdsRateLimit;
import io.sermant.core.service.xds.entity.XdsRequestCircuitBreakers;
import io.sermant.core.service.xds.entity.XdsRetryPolicy;

import java.util.Optional;

/**
 * xDS FlowControl service
 *
 * @author zhp
 * @since 2024-11-27
 **/
public interface XdsFlowControlService {
    /**
     * get Circuit breaker information for the client's request, When the number of active requests for an
     * instance reaches the specified limit, it will trigger a circuit breaker
     *
     * @param serviceName service name
     * @param clusterName cluster name
     * @return circuit breaker rules
     */
    Optional<XdsRequestCircuitBreakers> getRequestCircuitBreakers(String serviceName, String clusterName);

    /**
     * get Circuit breaker information of server instance, The instance has reached the specified number of errors
     * and will trigger a circuit breaker and the instance will be removed for a period of time
     *
     * @param serviceName service name
     * @param clusterName cluster name
     * @return Outlier Detection rules
     */
    Optional<XdsInstanceCircuitBreakers> getInstanceCircuitBreakers(String serviceName, String clusterName);

    /**
     * get retry policy of route name
     *
     * @param serviceName service name
     * @param routeName route name
     * @return retry policy
     */
    Optional<XdsRetryPolicy> getRetryPolicy(String serviceName, String routeName);

    /**
     * get rate limit of route name
     *
     * @param serviceName service name
     * @param routeName route name
     * @param port port
     * @return rate limit rule
     */
    Optional<XdsRateLimit> getRateLimit(String serviceName, String routeName, String port);

    /**
     * get http fault of route name
     *
     * @param serviceName service name
     * @param routeName route name
     * @return http fault rule
     */
    Optional<XdsHttpFault> getHttpFault(String serviceName, String routeName);
}
