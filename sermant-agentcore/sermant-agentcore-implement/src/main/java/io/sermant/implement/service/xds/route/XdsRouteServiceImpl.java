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

package io.sermant.implement.service.xds.route;

import io.sermant.core.service.xds.XdsRouteService;
import io.sermant.core.service.xds.entity.XdsRoute;
import io.sermant.implement.service.xds.cache.XdsDataCache;
import io.sermant.implement.service.xds.utils.XdsCommonUtils;

import java.util.List;

/**
 * XdsRouteService impl
 *
 * @author daizhenyu
 * @since 2024-05-08
 **/
public class XdsRouteServiceImpl implements XdsRouteService {
    /**
     * constructor
     */
    public XdsRouteServiceImpl() {
    }

    @Override
    public List<XdsRoute> getServiceRoute(String serviceName) {
        return XdsDataCache.getServiceRoute(serviceName);
    }

    @Override
    public boolean isLocalityRoute(String clusterName) {
        return XdsCommonUtils.getServiceNameFromCluster(clusterName)
                .map(serviceName -> XdsDataCache.isLocalityLb(serviceName, clusterName))
                .orElse(false);
    }
}
