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

import java.util.List;

/**
 * xDS RouteAction
 *
 * @author daizhenyu
 * @since 2024-08-05
 **/
public class XdsRouteAction {
    private String cluster;

    private boolean isWeighted = false;

    private XdsWeightedClusters weightedClusters;

    private XdsRetryPolicy retryPolicy;

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public boolean isWeighted() {
        return isWeighted;
    }

    public void setWeighted(boolean weighted) {
        isWeighted = weighted;
    }

    public XdsWeightedClusters getWeightedClusters() {
        return weightedClusters;
    }

    public void setWeightedClusters(XdsWeightedClusters weightedClusters) {
        this.weightedClusters = weightedClusters;
    }

    public XdsRetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(XdsRetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    /**
     * xDS WeightedClusters
     *
     * @author daizhenyu
     * @since 2024-08-05
     **/
    public static class XdsWeightedClusters {
        private List<XdsClusterWeight> clusters;

        private int totalWeight;

        public List<XdsClusterWeight> getClusters() {
            return clusters;
        }

        public void setClusters(List<XdsClusterWeight> clusters) {
            this.clusters = clusters;
        }

        public int getTotalWeight() {
            return totalWeight;
        }

        public void setTotalWeight(int totalWeight) {
            this.totalWeight = totalWeight;
        }
    }

    /**
     * xDS ClusterWeight
     *
     * @author daizhenyu
     * @since 2024-08-05
     **/
    public static class XdsClusterWeight {
        private String clusterName;

        private int weight;

        public String getClusterName() {
            return clusterName;
        }

        public void setClusterName(String clusterName) {
            this.clusterName = clusterName;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }
    }
}
