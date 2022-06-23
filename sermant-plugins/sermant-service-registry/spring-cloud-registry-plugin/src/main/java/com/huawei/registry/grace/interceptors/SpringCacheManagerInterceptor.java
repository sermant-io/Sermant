/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.grace.interceptors;

import com.huawei.registry.config.grace.GraceContext;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import org.springframework.cloud.loadbalancer.cache.LoadBalancerCacheManager;

/**
 * 注入请求拦截器, 针对loadbalancer缓存
 *
 * @author zhouss
 * @since 2022-05-23
 */
public class SpringCacheManagerInterceptor extends GraceSwitchInterceptor {
    @Override
    protected ExecuteContext doAfter(ExecuteContext context) {
        final Object object = context.getObject();
        if (object instanceof LoadBalancerCacheManager) {
            GraceContext.INSTANCE.getGraceShutDownManager().setLoadBalancerCacheManager(object);
        }
        return context;
    }

    @Override
    protected boolean isEnabled() {
        return super.isEnabled() && graceConfig.isEnableGraceShutdown();
    }
}
