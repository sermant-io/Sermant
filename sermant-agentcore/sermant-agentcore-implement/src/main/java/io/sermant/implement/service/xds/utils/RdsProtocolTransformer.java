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

package io.sermant.implement.service.xds.utils;

import io.envoyproxy.envoy.config.route.v3.HeaderMatcher;
import io.envoyproxy.envoy.config.route.v3.Route;
import io.envoyproxy.envoy.config.route.v3.RouteAction;
import io.envoyproxy.envoy.config.route.v3.RouteConfiguration;
import io.envoyproxy.envoy.config.route.v3.RouteMatch;
import io.envoyproxy.envoy.config.route.v3.VirtualHost;
import io.envoyproxy.envoy.config.route.v3.WeightedCluster;
import io.envoyproxy.envoy.config.route.v3.WeightedCluster.ClusterWeight;
import io.envoyproxy.envoy.type.matcher.v3.StringMatcher;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.service.xds.entity.XdsHeaderMatcher;
import io.sermant.core.service.xds.entity.XdsPathMatcher;
import io.sermant.core.service.xds.entity.XdsRoute;
import io.sermant.core.service.xds.entity.XdsRouteAction;
import io.sermant.core.service.xds.entity.XdsRouteAction.XdsClusterWeight;
import io.sermant.core.service.xds.entity.XdsRouteAction.XdsWeightedClusters;
import io.sermant.core.service.xds.entity.XdsRouteConfiguration;
import io.sermant.core.service.xds.entity.XdsRouteMatch;
import io.sermant.core.service.xds.entity.XdsVirtualHost;
import io.sermant.core.service.xds.entity.match.ExactMatchStrategy;
import io.sermant.core.service.xds.entity.match.PrefixMatchStrategy;
import io.sermant.core.service.xds.entity.match.PresentMatchStrategy;
import io.sermant.core.service.xds.entity.match.RegexMatchStrategy;
import io.sermant.core.service.xds.entity.match.SuffixMatchStrategy;
import io.sermant.core.service.xds.entity.match.UnknownMatchStrategy;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Convert rds protocol data to Sermant data model
 *
 * @author daizhenyu
 * @since 2024-08-22
 **/
public class RdsProtocolTransformer {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String POINT_SEPARATOR = "\\.";

    private RdsProtocolTransformer() {
    }

    /**
     * get XdsRouteConfiguration list
     *
     * @param routeConfigurations RouteConfiguration list
     * @return XdsRouteConfiguration list
     */
    public static List<XdsRouteConfiguration> getRouteConfigurations(List<RouteConfiguration> routeConfigurations) {
        return routeConfigurations.stream()
                .filter(Objects::nonNull)
                .map(routeConfiguration -> parseRouteConfiguration(routeConfiguration))
                .collect(Collectors.toList());
    }

    private static XdsRouteConfiguration parseRouteConfiguration(RouteConfiguration routeConfiguration) {
        XdsRouteConfiguration xdsRouteConfiguration = new XdsRouteConfiguration();
        xdsRouteConfiguration.setRouteConfigName(routeConfiguration.getName());
        Map<String, XdsVirtualHost> xdsVirtualHostMap = routeConfiguration.getVirtualHostsList().stream()
                .map(virtualHost -> parseVirtualHost(virtualHost))
                .collect(Collectors.toMap(
                        xdsVirtualHost -> xdsVirtualHost.getName().split(POINT_SEPARATOR)[0],
                        xdsVirtualHost -> xdsVirtualHost
                ));
        xdsRouteConfiguration.setVirtualHosts(xdsVirtualHostMap);
        return xdsRouteConfiguration;
    }

    private static XdsVirtualHost parseVirtualHost(VirtualHost virtualHost) {
        XdsVirtualHost xdsVirtualHost = new XdsVirtualHost();
        List<String> domains = virtualHost.getDomainsList();
        List<XdsRoute> xdsRoutes =
                virtualHost.getRoutesList().stream()
                        .map(route -> parseRoute(route))
                        .collect(Collectors.toList());
        xdsVirtualHost.setName(virtualHost.getName());
        xdsVirtualHost.setRoutes(xdsRoutes);
        xdsVirtualHost.setDomains(domains);
        return xdsVirtualHost;
    }

    private static XdsRoute parseRoute(Route route) {
        XdsRoute xdsRoute = new XdsRoute();
        xdsRoute.setName(route.getName());
        xdsRoute.setRouteMatch(parseRouteMatch(route.getMatch()));
        xdsRoute.setRouteAction(parseRouteAction(route.getRoute()));
        return xdsRoute;
    }

    private static XdsRouteMatch parseRouteMatch(RouteMatch routeMatch) {
        XdsRouteMatch xdsRouteMatch = new XdsRouteMatch();
        xdsRouteMatch.setPathMatcher(parsePathMatcher(routeMatch));
        xdsRouteMatch.setHeaderMatchers(parseHeaderMatchers(routeMatch.getHeadersList()));
        xdsRouteMatch.setCaseSensitive(routeMatch.getCaseSensitive().getValue());
        return xdsRouteMatch;
    }

    private static List<XdsHeaderMatcher> parseHeaderMatchers(List<HeaderMatcher> headerMatchers) {
        return headerMatchers.stream()
                .filter(Objects::nonNull)
                .map(headerMatcher -> parseHeaderMatcher(headerMatcher))
                .collect(Collectors.toList());
    }

    private static XdsHeaderMatcher parseHeaderMatcher(HeaderMatcher headerMatcher) {
        if (headerMatcher.getPresentMatch()) {
            return new XdsHeaderMatcher(headerMatcher.getName(),
                    new PresentMatchStrategy());
        }
        if (!headerMatcher.hasStringMatch()) {
            LOGGER.log(Level.WARNING,
                    "The xDS route header matching strategy is unknown. Please check the route configuration.");
            return new XdsHeaderMatcher(headerMatcher.getName(),
                    new UnknownMatchStrategy());
        }
        StringMatcher stringMatch = headerMatcher.getStringMatch();
        switch (stringMatch.getMatchPatternCase()) {
            case EXACT:
                return new XdsHeaderMatcher(headerMatcher.getName(),
                        new ExactMatchStrategy(stringMatch.getExact()));
            case PREFIX:
                return new XdsHeaderMatcher(headerMatcher.getName(),
                        new PrefixMatchStrategy(stringMatch.getPrefix()));
            case SUFFIX:
                return new XdsHeaderMatcher(headerMatcher.getName(),
                        new SuffixMatchStrategy(stringMatch.getSuffix()));
            case SAFE_REGEX:
                return new XdsHeaderMatcher(headerMatcher.getName(),
                        new RegexMatchStrategy(stringMatch.getSafeRegex().getRegex()));
            default:
                LOGGER.log(Level.WARNING,
                        "The xDS route header matching strategy is unknown. Please check the route configuration.");
                return new XdsHeaderMatcher(headerMatcher.getName(),
                        new UnknownMatchStrategy());
        }
    }

    private static XdsPathMatcher parsePathMatcher(RouteMatch routeMatch) {
        boolean caseSensitive = routeMatch.getCaseSensitive().getValue();
        switch (routeMatch.getPathSpecifierCase()) {
            case PATH:
                String path = routeMatch.getPath();
                return new XdsPathMatcher(new ExactMatchStrategy(caseSensitive ? path : path.toLowerCase(Locale.ROOT)),
                        caseSensitive);
            case PREFIX:
                String prefix = routeMatch.getPrefix();
                return new XdsPathMatcher(
                        new PrefixMatchStrategy(caseSensitive ? prefix : prefix.toLowerCase(Locale.ROOT)),
                        caseSensitive);
            default:
                LOGGER.log(Level.WARNING,
                        "The xDS route path matching strategy is unknown. Please check the route configuration.");
                return new XdsPathMatcher(new UnknownMatchStrategy(), caseSensitive);
        }
    }

    private static XdsRouteAction parseRouteAction(RouteAction routeAction) {
        XdsRouteAction xdsRouteAction = new XdsRouteAction();
        switch (routeAction.getClusterSpecifierCase()) {
            case CLUSTER:
                xdsRouteAction.setCluster(routeAction.getCluster());
                break;
            case WEIGHTED_CLUSTERS:
                xdsRouteAction.setWeighted(true);
                xdsRouteAction.setWeightedClusters(parseWeightedClusters(routeAction.getWeightedClusters()));
                break;
            default:
                LOGGER.log(Level.WARNING,
                        "The xDS route action strategy is unknown. Please check the route configuration.");
        }
        return xdsRouteAction;
    }

    private static XdsWeightedClusters parseWeightedClusters(WeightedCluster clusters) {
        XdsWeightedClusters xdsWeightedClusters = new XdsWeightedClusters();
        xdsWeightedClusters.setTotalWeight(clusters.getClustersList().stream()
                .filter(Objects::nonNull)
                .mapToInt(clusterWeight -> clusterWeight.getWeight().getValue())
                .sum()
        );
        xdsWeightedClusters.setClusters(clusters.getClustersList().stream()
                .filter(Objects::nonNull)
                .map(clusterWeight -> parseClusterWeight(clusterWeight))
                .collect(Collectors.toList()));
        return xdsWeightedClusters;
    }

    private static XdsClusterWeight parseClusterWeight(ClusterWeight clusterWeight) {
        XdsClusterWeight xdsClusterWeight = new XdsClusterWeight();
        xdsClusterWeight.setWeight(clusterWeight.getWeight().getValue());
        xdsClusterWeight.setClusterName(clusterWeight.getName());
        return xdsClusterWeight;
    }
}
