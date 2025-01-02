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

package io.sermant.discovery.service.lb.rule;

import io.sermant.core.utils.StringUtils;
import io.sermant.discovery.entity.ServiceInstance;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * nacos Weight
 *
 * @author xiaozhao
 * @since 2024-11-16
 */
public class NacosWeightRandomRule extends AbstractLoadbalancer {
    @Override
    protected ServiceInstance doChoose(String serviceName, List<ServiceInstance> instances) {
        List<InstanceWithWeight> withWeights = instances.stream()
                .map(instance -> {
                    String nacosWeight = instance.getMetadata().get("nacos.weight");
                    int weight = StringUtils.isBlank(nacosWeight) ? 1 : Integer.parseInt(nacosWeight);
                    return new InstanceWithWeight(instance, weight);
                }).collect(Collectors.toList());
        return this.weightRandom(withWeights);
    }

    @Override
    public String lbType() {
        return "NacosWeight";
    }

    /**
     * nacos Weight
     *
     * @author xiaozhao
     * @since 2024-11-16
     */
    public static class InstanceWithWeight {
        private ServiceInstance server;
        private Integer weight;

        /**
         * constructor
         *
         * @param instance instance
         * @param weight weight
         */
        public InstanceWithWeight(ServiceInstance instance, int weight) {
            this.server = instance;
            this.weight = weight;
        }

        public ServiceInstance getServer() {
            return server;
        }

        public Integer getWeight() {
            return weight;
        }
    }

    /**
     * Random by weight
     *
     * @param list instance list
     * @return just a random result
     * @throws IllegalArgumentException The parameter error is abnormal
     */
    public ServiceInstance weightRandom(List<InstanceWithWeight> list) {
        int totalWeight = list.stream().mapToInt(InstanceWithWeight::getWeight).sum();
        int randomWeight = new Random().nextInt(totalWeight);
        int currentWeight = 0;
        for (InstanceWithWeight instanceWithWeight : list) {
            currentWeight += instanceWithWeight.getWeight();
            if (randomWeight < currentWeight) {
                return instanceWithWeight.getServer();
            }
        }
        throw new IllegalArgumentException("Should never reach here if weight logic is correct");
    }
}
