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

package com.huawei.register.interceptors.health;

import com.huawei.register.context.RegisterContext;
import com.huawei.sermant.core.agent.interceptor.ConstructorInterceptor;

/**
 * 拦截ConsulCatalogWatch,用于后续关闭服务
 *
 * @author zhouss
 * @since 2021-12-31
 */
public class ConsulWatchInterceptor implements ConstructorInterceptor {
    @Override
    public void onConstruct(Object obj, Object[] allArguments) {
        RegisterContext.INSTANCE.setRegisterWatch(obj);
    }
}
