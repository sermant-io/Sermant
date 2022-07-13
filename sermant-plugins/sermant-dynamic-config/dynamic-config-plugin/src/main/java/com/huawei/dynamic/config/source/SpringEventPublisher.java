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

package com.huawei.dynamic.config.source;

import com.huawei.dynamic.config.ConfigHolder;

import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;

import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

/**
 * spring事件发布器
 *
 * @author zhouss
 * @since 2022-04-08
 */
@Component
public class SpringEventPublisher implements ApplicationEventPublisherAware {
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
        initDynamicConfig();
    }

    private void initDynamicConfig() {
        ConfigHolder.INSTANCE.addListener(this::publishRefreshEvent);
    }

    /**
     * 发布spring刷新事件{@link org.springframework.cloud.endpoint.event.RefreshEvent}
     */
    private void publishRefreshEvent(DynamicConfigEvent event) {
        if (event.getEventType() == DynamicConfigEventType.INIT) {
            return;
        }
        applicationEventPublisher.publishEvent(new RefreshEvent(this, null, "sermant refresh"));
    }
}
