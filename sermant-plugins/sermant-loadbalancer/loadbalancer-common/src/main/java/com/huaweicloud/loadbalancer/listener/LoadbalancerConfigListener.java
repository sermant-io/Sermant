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

package com.huaweicloud.loadbalancer.listener;

import com.huaweicloud.loadbalancer.config.DubboLoadbalancerType;
import com.huaweicloud.loadbalancer.config.LoadbalancerConfig;
import com.huaweicloud.loadbalancer.config.RibbonLoadbalancerType;
import com.huaweicloud.loadbalancer.config.SpringLoadbalancerType;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import com.huaweicloud.sermant.core.utils.StringUtils;

import org.yaml.snakeyaml.Yaml;

import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 配置监听器
 *
 * @author pengyuyi
 * @since 2022-01-22
 */
public class LoadbalancerConfigListener implements DynamicConfigListener {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String DUBBO_TYPE_KEY = "dubboType";

    private static final String SPRING_TYPE_KEY = "springType";

    private static final String RIBBON_TYPE_KEY = "ribbonType";

    private final LoadbalancerConfig config;

    private final DubboLoadbalancerType defaultDubboType;

    private final SpringLoadbalancerType defaultSpringType;

    private final RibbonLoadbalancerType defaultRibbonType;

    /**
     * 构造方法
     *
     * @param config 配置
     */
    public LoadbalancerConfigListener(LoadbalancerConfig config) {
        this.config = config;
        this.defaultDubboType = config.getDubboType();
        this.defaultSpringType = config.getSpringType();
        this.defaultRibbonType = config.getRibbonType();
    }

    @Override
    public void process(DynamicConfigEvent event) {
        String key = event.getKey();
        if (key == null || !key.equals(config.getKey()) || StringUtils.isBlank(event.getContent())) {
            return;
        }
        if (event.getEventType() == DynamicConfigEventType.DELETE) {
            // 如果删除了，就恢复默认的配置
            config.setDubboType(defaultDubboType);
            config.setSpringType(defaultSpringType);
            config.setRibbonType(defaultRibbonType);
            return;
        }
        Map<String, String> load = new Yaml().load(event.getContent());
        String dubboType = load.get(DUBBO_TYPE_KEY);
        if (StringUtils.isExist(dubboType)) {
            config.setDubboType(DubboLoadbalancerType.valueOf(dubboType.toUpperCase(Locale.ROOT)));
        }
        String springType = load.get(SPRING_TYPE_KEY);
        if (StringUtils.isExist(springType)) {
            config.setSpringType(SpringLoadbalancerType.valueOf(springType.toUpperCase(Locale.ROOT)));
        }
        String ribbonType = load.get(RIBBON_TYPE_KEY);
        if (StringUtils.isExist(ribbonType)) {
            config.setRibbonType(RibbonLoadbalancerType.valueOf(ribbonType.toUpperCase(Locale.ROOT)));
        }
        LOGGER.info(String.format(Locale.ROOT, "Config [%s] has been %s ", key, event.getEventType()));
    }
}
