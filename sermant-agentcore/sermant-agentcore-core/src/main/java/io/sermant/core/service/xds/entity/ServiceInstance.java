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

package io.sermant.core.service.xds.entity;

import java.util.Map;

/**
 * service instance interface
 *
 * @author daizhenyu
 * @since 2024-05-09
 **/
public interface ServiceInstance {
    /**
     * get xds cluster name
     *
     * @return cluster name
     */
    String getClusterName();

    /**
     * get service name
     *
     * @return service name
     */
    String getServiceName();

    /**
     * get service instance host
     *
     * @return service instance host
     */
    String getHost();

    /**
     * get service instance port
     *
     * @return service instance port
     */
    int getPort();

    /**
     * get service instance metadata
     *
     * @return metadata
     */
    Map<String, String> getMetaData();

    /**
     * get service instance health status
     *
     * @return service instance health status
     */
    boolean isHealthy();
}
