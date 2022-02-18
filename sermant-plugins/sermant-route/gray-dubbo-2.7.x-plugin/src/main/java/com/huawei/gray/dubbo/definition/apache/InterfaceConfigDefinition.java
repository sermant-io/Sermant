/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.gray.dubbo.definition.apache;

import com.huawei.gray.dubbo.definition.AbstractInstDefinition;

/**
 * 增强AbstractInterfaceConfig类的getApplication方法，用来获取应用名
 *
 * @author provenceee
 * @since 2021年11月8日
 */
public class InterfaceConfigDefinition extends AbstractInstDefinition {
    private static final String ENHANCE_CLASS = "org.apache.dubbo.config.AbstractInterfaceConfig";

    private static final String INTERCEPT_CLASS = "com.huawei.gray.dubbo.interceptor.apache.InterfaceConfigInterceptor";

    private static final String METHOD_NAME = "getApplication";

    public InterfaceConfigDefinition() {
        super(ENHANCE_CLASS, INTERCEPT_CLASS, METHOD_NAME);
    }
}