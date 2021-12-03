/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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
 * 增强MonitorFilter类的invoke方法，更改路由信息
 *
 * @author pengyuyi
 * @since 2021年6月28日
 */
public class MonitorFilterDefinition extends AbstractInstDefinition {
    private static final String ENHANCE_CLASS = "org.apache.dubbo.monitor.support.MonitorFilter";

    private static final String INTERCEPT_CLASS = "com.huawei.gray.dubbo.interceptor.apache.MonitorFilterInterceptor";

    private static final String METHOD_NAME = "invoke";

    public MonitorFilterDefinition() {
        super(ENHANCE_CLASS, INTERCEPT_CLASS, METHOD_NAME);
    }
}
