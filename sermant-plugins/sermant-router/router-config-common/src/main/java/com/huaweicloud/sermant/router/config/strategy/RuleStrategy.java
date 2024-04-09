/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.sermant.router.config.strategy;

import com.huaweicloud.sermant.router.config.entity.Rule;

import java.util.List;
import java.util.Map;

/**
 * Routing policy
 *
 * @param <I> Instance generics
 * @author provenceee
 * @since 2021-10-14
 */
public interface RuleStrategy<I> {
    /**
     * Select an instance of the route
     *
     * @param serviceName Service name
     * @param instances List of instances
     * @param rule Routing rules
     * @return Instances that are route-filtered
     */
    List<I> getFlowMatchInstances(String serviceName, List<I> instances, Rule rule);

    /**
     * Select an instance of the route
     *
     * @param serviceName Service name
     * @param instances List of instances
     * @param rule Rules
     * @return Instances filtered by rules
     */
    List<I> getMatchInstances(String serviceName, List<I> instances, Rule rule);

    /**
     * Select an instance of the route based on the request information
     *
     * @param serviceName Service name
     * @param instances List of instances
     * @param tags Request information
     * @return Instances that are route-filtered
     */
    List<I> getMatchInstancesByRequest(String serviceName, List<I> instances, Map<String, String> tags);

    /**
     * Select instances of mismatched labels
     *
     * @param serviceName Service name
     * @param instances List of instances
     * @param tags Label
     * @param isReturnAllInstancesWhenMismatch If there is no match, whether to return all instances
     * @return Instances that are route-filtered
     */
    List<I> getMismatchInstances(String serviceName, List<I> instances, List<Map<String, String>> tags,
            boolean isReturnAllInstancesWhenMismatch);
}