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

package io.sermant.flowcontrol.common.core.match;

import io.sermant.core.service.xds.entity.XdsHeaderMatcher;
import io.sermant.core.service.xds.entity.XdsPathMatcher;
import io.sermant.core.service.xds.entity.XdsRoute;
import io.sermant.core.service.xds.entity.XdsRouteAction;
import io.sermant.core.service.xds.entity.XdsRouteAction.XdsClusterWeight;
import io.sermant.core.service.xds.entity.XdsRouteAction.XdsWeightedClusters;
import io.sermant.core.service.xds.entity.XdsRouteMatch;
import io.sermant.core.utils.CollectionUtils;
import io.sermant.core.utils.StringUtils;
import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.common.entity.RequestEntity;
import io.sermant.flowcontrol.common.util.RandomUtil;
import io.sermant.flowcontrol.common.xds.handler.XdsHandler;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * XdsRouteMatchManager, Get BusinessEntity based on xDS routing rules
 *
 * @author zhp
 * @since 2024-12-20
 **/
public enum XdsRouteMatchManager {
    /**
     * singleton
     */
    INSTANCE;

    /**
     * get matched scenario information
     *
     * @param requestEntity request-information
     * @param serviceName service name
     * @return matched business information
     */
    public FlowControlScenario getMatchedScenarioInfo(RequestEntity requestEntity, String serviceName) {
        FlowControlScenario scenario = new FlowControlScenario();
        scenario.setServiceName(serviceName);
        Optional<XdsRoute> matchedRouteOptional = getMatchedRoute(requestEntity, serviceName);
        if (!matchedRouteOptional.isPresent()) {
            return scenario;
        }
        XdsRoute matchedRoute = matchedRouteOptional.get();
        scenario.setRouteName(matchedRoute.getName());
        scenario.setClusterName(selectClusterByRoute(matchedRoute));
        return scenario;
    }

    private Optional<XdsRoute> getMatchedRoute(RequestEntity requestEntity, String serviceName) {
        List<XdsRoute> routes =
                XdsHandler.INSTANCE.getServiceRouteByServiceName(serviceName);
        for (XdsRoute route : routes) {
            XdsRouteMatch routeMatch = route.getRouteMatch();

            // check path matching
            if (!isPathMatched(routeMatch.getPathMatcher(), requestEntity.getApiPath())) {
                continue;
            }

            // check head matching
            if (!isHeadersMatched(routeMatch.getHeaderMatchers(), requestEntity.getHeaders())) {
                continue;
            }
            return Optional.of(route);
        }
        return Optional.empty();
    }

    private boolean isPathMatched(XdsPathMatcher matcher, String path) {
        return matcher.isMatch(path);
    }

    private boolean isHeadersMatched(List<XdsHeaderMatcher> matchers, Map<String, String> headers) {
        for (XdsHeaderMatcher xdsHeaderMatcher : matchers) {
            if (!xdsHeaderMatcher.isMatch(headers)) {
                return false;
            }
        }
        return true;
    }

    private String selectClusterByRoute(XdsRoute matchedRoute) {
        XdsRouteAction routeAction = matchedRoute.getRouteAction();
        String cluster = routeAction.getCluster();
        if (!routeAction.isWeighted() || routeAction.getWeightedClusters() == null) {
            return cluster;
        }
        XdsWeightedClusters weightedClusters = routeAction.getWeightedClusters();
        List<XdsClusterWeight> clusters = weightedClusters.getClusters();
        int totalWeight = weightedClusters.getTotalWeight();
        if (CollectionUtils.isEmpty(clusters) || totalWeight == 0) {
            return StringUtils.EMPTY;
        }
        int randomWeight = RandomUtil.randomInt(totalWeight);

        int currentWeight = 0;
        for (XdsClusterWeight clusterWeight : clusters) {
            currentWeight += clusterWeight.getWeight();
            if (randomWeight < currentWeight) {
                return clusterWeight.getClusterName();
            }
        }
        return StringUtils.EMPTY;
    }
}
