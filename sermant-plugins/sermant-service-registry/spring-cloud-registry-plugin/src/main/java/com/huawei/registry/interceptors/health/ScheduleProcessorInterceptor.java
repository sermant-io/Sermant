/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.support.RegisterSwitchSupport;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

/**
 * 拦截获取org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor
 *
 * @author zhouss
 * @since 2022-06-14
 */
public class ScheduleProcessorInterceptor extends RegisterSwitchSupport {
    @Override
    protected ExecuteContext doAfter(ExecuteContext context) {
        RegisterContext.INSTANCE.setScheduleProcessor(context.getResult());
        return context;
    }
}
