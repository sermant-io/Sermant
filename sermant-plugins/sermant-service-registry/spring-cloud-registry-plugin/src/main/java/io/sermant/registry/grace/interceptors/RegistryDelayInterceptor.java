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

package io.sermant.registry.grace.interceptors;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.utils.StringUtils;
import io.sermant.registry.config.grace.GraceContext;
import io.sermant.registry.grace.declarers.RegistryDelayDeclarer;
import io.sermant.registry.support.RegistryDelayConsumer;
import io.sermant.registry.utils.CommonUtils;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Register for Delay Interception
 *
 * @author zhouss
 * @since 2022-05-17
 */
public class RegistryDelayInterceptor extends GraceSwitchInterceptor {
    private final AtomicBoolean isDelayed = new AtomicBoolean();

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        if (isDelayed.compareAndSet(false, true) && graceConfig.getStartDelayTime() > 0) {
            LoggerFactory.getLogger().info(String.format(Locale.ENGLISH,
                    "[Origin Register Center] registry start delay at [%s]", LocalDateTime.now()));
            CommonUtils.accept(new RegistryDelayConsumer(), graceConfig.getStartDelayTime());
            LoggerFactory.getLogger().info(String.format(Locale.ENGLISH,
                    "[Origin Register Center] registry end delay at [%s]", LocalDateTime.now()));
        }
        return context;
    }

    @Override
    protected ExecuteContext doAfter(ExecuteContext context) {
        // Record when the registration was completed
        final long currentTimeMillis = System.currentTimeMillis();
        GraceContext.INSTANCE.setRegistryFinishTime(currentTimeMillis);
        final Object object = context.getObject();
        if (!StringUtils.equals(RegistryDelayDeclarer.OLD_VERSION_ENHANCE_CLASS, object.getClass().getName())) {
            GraceContext.INSTANCE.getGraceShutDownManager().setRegistration(object);
        }
        return context;
    }
}
