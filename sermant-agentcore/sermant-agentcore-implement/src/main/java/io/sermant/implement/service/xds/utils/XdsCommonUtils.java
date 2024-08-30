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

import io.sermant.core.utils.StringUtils;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * xds common utils
 *
 * @author daizhenyu
 * @since 2024-08-22
 **/
public class XdsCommonUtils {
    private static final int SERVICE_HOST_INDEX = 3;

    private static final int SERVICE_NAME_INDEX = 0;

    private static final String VERTICAL_LINE_SEPARATOR = "\\|";

    private static final String POINT_SEPARATOR = "\\.";

    private static final Pattern CLUSTER_NAME_FORMAT = Pattern.compile("^\\w+\\|\\w+\\|[^|]*\\|[^|]+$");

    private XdsCommonUtils() {
    }

    /***
     * get service name from cluster
     *
     * @param clusterName cluster name
     * @return service name
     */
    public static Optional<String> getServiceNameFromCluster(String clusterName) {
        if (StringUtils.isEmpty(clusterName) || !CLUSTER_NAME_FORMAT.matcher(clusterName).matches()) {
            return Optional.empty();
        }

        // cluster name format: "outbound|8080||xds-service.default.svc.cluster.local", xds-service is service name
        String[] clusterSplit = clusterName.split(VERTICAL_LINE_SEPARATOR);
        return Optional.of(clusterSplit[SERVICE_HOST_INDEX].split(POINT_SEPARATOR)[SERVICE_NAME_INDEX]);
    }
}
