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

package com.huawei.dubbo.register.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * dubbo条件注解
 *
 * @author provenceee
 * @date 2021/12/16
 */
public class ConditionOnDubbo implements Condition {
    private static final String CLASS_NAME = "com.huawei.dubbo.register.ServiceCenterRegistry";

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        return isLoadDubbo();
    }

    /**
     * 是否加载了dubbo相关的类
     *
     * @return 是否加载了dubbo相关的类
     */
    public static boolean isLoadDubbo() {
        try {
            Class.forName(CLASS_NAME);
        } catch (Throwable throwable) {
            return false;
        }
        return true;
    }
}