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

package io.sermant.router.common.xds.lb;

import io.sermant.core.service.xds.entity.ServiceInstance;

import java.util.List;

/**
 * XdsLoadBalancer
 *
 * @author daizhenyu
 * @since 2024-08-30
 **/
public interface XdsLoadBalancer {
    /**
     * select instance by loadbalancer
     *
     * @param instances service instance
     * @return selected instance
     */
    ServiceInstance selectInstance(List<ServiceInstance> instances);
}
