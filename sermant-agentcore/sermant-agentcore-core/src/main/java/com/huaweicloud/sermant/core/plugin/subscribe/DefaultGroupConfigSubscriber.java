/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.sermant.core.plugin.subscribe;

import com.huaweicloud.sermant.core.plugin.subscribe.processor.DefaultConfigProcessor;
import com.huaweicloud.sermant.core.plugin.subscribe.processor.IntegratedEventListenerAdapter;
import com.huaweicloud.sermant.core.service.dynamicconfig.DynamicConfigService;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import com.huaweicloud.sermant.core.utils.LabelGroupUtils;
import com.huaweicloud.sermant.core.utils.StringUtils;

import java.util.Collections;
import java.util.Map;

/**
 * Group configuration subscription is configured by default based on the service name
 *
 * @author zhouss
 * @since 2022-04-14
 */
public class DefaultGroupConfigSubscriber extends AbstractGroupConfigSubscriber {
    private final String serviceName;

    private final DynamicConfigListener listener;

    /**
     * Default configuration subscription
     *
     * @param serviceName service name
     * @param listener listener
     * @param pluginName plugin name
     */
    public DefaultGroupConfigSubscriber(String serviceName, DynamicConfigListener listener, String pluginName) {
        this(serviceName, listener, null, pluginName);
    }

    /**
     * Custom configuration center implementation
     *
     * @param serviceName service name
     * @param listener listener
     * @param dynamicConfigService Configuration center implementation
     * @param pluginName plugin name
     */
    public DefaultGroupConfigSubscriber(String serviceName, DynamicConfigListener listener,
            DynamicConfigService dynamicConfigService, String pluginName) {
        super(dynamicConfigService, pluginName);
        this.serviceName = serviceName;
        this.listener = listener;
    }

    @Override
    protected Map<String, DynamicConfigListener> buildGroupSubscribers() {
        return Collections
                .singletonMap(LabelGroupUtils.createLabelGroup(Collections.singletonMap("service", serviceName)),
                        new IntegratedEventListenerAdapter(new DefaultConfigProcessor(listener), null));
    }

    @Override
    protected boolean isReady() {
        return !StringUtils.isBlank(serviceName);
    }
}
