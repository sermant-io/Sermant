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

import io.sermant.core.service.BaseService;

/**
 * xDS core service interface, to start or stop xDS service, or get the specific xDS capability implementation class
 *
 * @author daizhenyu
 * @since 2024-05-21
 **/
public interface XdsCoreService extends BaseService {
    /**
     * get xDS service discovery
     *
     * @return XdsServiceDiscovery
     */
    XdsServiceDiscovery getXdsServiceDiscovery();

    /**
     * get xDS route service
     *
     * @return XdsRoute
     */
    XdsRouteService getXdsRouteService();

    /**
     * get xDS lb service
     *
     * @return XdsRoute
     */
    XdsLoadBalanceService getLoadBalanceService();
}
