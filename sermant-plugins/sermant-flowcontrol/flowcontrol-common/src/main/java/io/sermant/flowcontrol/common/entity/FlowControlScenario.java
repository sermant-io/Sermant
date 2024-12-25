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

package io.sermant.flowcontrol.common.entity;

import java.util.Set;

/**
 * Scenario information for flow control, primarily used to retrieve matching flow control rules
 *
 * @author zhp
 * @since 2024-11-27
 */
public class FlowControlScenario {
    /**
     * matched service scenario name
     */
    private Set<String> matchedScenarioNames;

    /**
     * The name of the downstream service
     */
    private String serviceName;

    /**
     * cluster name
     */
    private String clusterName;

    /**
     * route rule name
     */
    private String routeName;

    /**
     * request Address,ip:port
     */
    private String address;

    public Set<String> getMatchedScenarioNames() {
        return matchedScenarioNames;
    }

    public void setMatchedScenarioNames(Set<String> matchedScenarioNames) {
        this.matchedScenarioNames = matchedScenarioNames;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
