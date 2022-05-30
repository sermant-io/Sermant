/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.registry.interceptors.health;

import com.huawei.registry.config.RegisterDynamicConfig;
import com.huawei.registry.support.RegisterSwitchSupport;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import org.springframework.boot.actuate.health.Health;

/**
 * 若采用单注册或者已下发关闭原注册中心, 则不需再进行健康检查
 *
 * @author zhouss
 * @since 2022-05-22
 */
public class HealthIndicatorInterceptor extends RegisterSwitchSupport {
    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        if (RegisterDynamicConfig.INSTANCE.isNeedCloseOriginRegisterCenter()) {
            context.skip(Health.down().build());
        }
        return context;
    }
}
