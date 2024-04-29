/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.sermant.loadbalancer.config;

import io.sermant.core.config.common.ConfigTypeKey;
import io.sermant.core.plugin.config.PluginConfig;

/**
 * load balancing configuration
 *
 * @author provenceee
 * @since 2022-01-21
 */
@ConfigTypeKey("loadbalancer.plugin")
public class LoadbalancerConfig implements PluginConfig {
    /**
     * dubbo load balance strategy
     */
    private DubboLoadbalancerType dubboType = DubboLoadbalancerType.RANDOM;

    /**
     * spring cloud loadbalancer load balance strategy
     */
    private SpringLoadbalancerType springType = SpringLoadbalancerType.ROUND_ROBIN;

    /**
     * ribbon load balance strategy
     */
    private RibbonLoadbalancerType ribbonType = RibbonLoadbalancerType.ROUND_ROBIN;

    /**
     * default load balance strategy
     */
    private String defaultRule;

    /**
     * whether to use cse rules
     */
    private boolean useCseRule = true;

    /**
     * Whether to force the use of plugin load balancing, the current configuration only takes effect on the ribbon.
     * Ribbon may have the user's own load balancing key. If the user does not want to affect his own load balancing
     * key, he can set it to false.
     */
    private boolean forceUseSermantLb = true;

    public boolean isForceUseSermantLb() {
        return forceUseSermantLb;
    }

    public boolean isUseCseRule() {
        return useCseRule;
    }

    public void setUseCseRule(boolean useCseRule) {
        this.useCseRule = useCseRule;
    }

    public DubboLoadbalancerType getDubboType() {
        return dubboType;
    }

    public void setDubboType(DubboLoadbalancerType dubboType) {
        this.dubboType = dubboType;
    }

    public SpringLoadbalancerType getSpringType() {
        return springType;
    }

    public void setSpringType(SpringLoadbalancerType springType) {
        this.springType = springType;
    }

    public RibbonLoadbalancerType getRibbonType() {
        return ribbonType;
    }

    public String getDefaultRule() {
        return defaultRule;
    }

    public void setDefaultRule(String defaultRule) {
        this.defaultRule = defaultRule;
    }

    public void setRibbonType(RibbonLoadbalancerType ribbonType) {
        this.ribbonType = ribbonType;
    }
}
