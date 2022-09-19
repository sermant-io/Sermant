/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.flowcontrol;

import com.huawei.flowcontrol.common.config.CommonConst;
import com.huawei.flowcontrol.service.InterceptorSupporter;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 拦截ClusterUtils#mergeUrl,每当发现下游服务时, 该方法则会被调用. 此处需取出接口与下游的映射关系
 *
 * @author zhouss
 * @since 2022-09-13
 */
public class ClusterInterceptor extends InterceptorSupporter {
    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        final Object url = context.getArguments()[0];
        final Map<String, String> parameters = getParameters(url);
        final String application = parameters.get(CommonConst.DUBBO_APPLICATION);
        final String interfaceName = parameters.get(CommonConst.DUBBO_INTERFACE);
        if (Objects.nonNull(application) && Objects.nonNull(interfaceName)) {
            DubboApplicationCache.INSTANCE.cache(interfaceName, application);
        }
        return context;
    }

    private Map<String, String> getParameters(Object url) {
        final Optional<Object> parameters = ReflectUtils.invokeMethod(url, "getParameters", null, null);
        if (!parameters.isPresent()) {
            return Collections.emptyMap();
        }
        final Object paramMap = parameters.get();
        if (paramMap instanceof Map) {
            return (Map<String, String>) paramMap;
        }
        return Collections.emptyMap();
    }

    @Override
    protected ExecuteContext doAfter(ExecuteContext context) throws Exception {
        return context;
    }
}
