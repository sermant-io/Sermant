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

package com.huawei.flowcontrol.retry;

import com.huawei.flowcontrol.retry.cluster.ClusterInvokerCreator;
import com.huawei.flowcontrol.service.InterceptorSupporter;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ExtensionLoader 拦截器， 用于注入cluster
 *
 * @author zhouss
 * @since 2022-03-04
 */
public class ExtensionLoaderGetExtensionInterceptor extends InterceptorSupporter {
    private final AtomicBoolean isCache = new AtomicBoolean();

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        final Class<?> type = (Class<?>) context.getMemberFieldValue("type");
        if (type != null && canInjectClusterInvoker(type.getName())) {
            backUpOriginType((String) context.getArguments()[0]);
            context.getArguments()[0] = flowControlConfig.getRetryClusterInvoker();
            return context;
        }
        return context;
    }

    private void backUpOriginType(String name) {
        if (isCache.compareAndSet(false, true)) {
            ClusterInvokerCreator.INSTANCE.setOriginInvokerName(name);
        }
    }

    @Override
    protected ExecuteContext doAfter(ExecuteContext context) {
        return context;
    }
}
