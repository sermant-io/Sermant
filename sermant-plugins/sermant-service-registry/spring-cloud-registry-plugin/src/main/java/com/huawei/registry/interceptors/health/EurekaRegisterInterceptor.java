/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.registry.interceptors.health;

import com.huawei.registry.config.RegisterDynamicConfig;
import com.huawei.registry.support.RegisterSwitchSupport;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

/**
 * Eureka timer injection entry interception
 *
 * @author zhouss
 * @since 2022-04-11
 */
public class EurekaRegisterInterceptor extends RegisterSwitchSupport {
    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        if (!registerConfig.isOpenMigration() || RegisterDynamicConfig.INSTANCE.isNeedCloseOriginRegisterCenter()) {
            context.skip(true);
        }
        return context;
    }
}
