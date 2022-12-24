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

package com.huaweicloud.sermant.router.dubbo.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.router.common.request.RequestHeader;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;
import com.huaweicloud.sermant.router.dubbo.service.DubboConfigService;
import com.huaweicloud.sermant.router.dubbo.utils.DubboReflectUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 增强ContextFilter类的invoke方法
 *
 * @author provenceee
 * @since 2022-09-26
 */
public class ContextFilterInterceptor extends AbstractInterceptor {
    private final DubboConfigService configService;

    /**
     * 构造方法
     */
    public ContextFilterInterceptor() {
        configService = ServiceManager.getService(DubboConfigService.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        Set<String> matchKeys = configService.getMatchKeys();
        if (CollectionUtils.isEmpty(matchKeys)) {
            return context;
        }
        Map<String, Object> attachments = DubboReflectUtils.getAttachments(context.getArguments()[1]);
        Map<String, List<String>> header = new HashMap<>();
        matchKeys.forEach(key -> {
            if (attachments.containsKey(key)) {
                String value = Optional.ofNullable(attachments.get(key)).map(String::valueOf).orElse(null);
                header.put(key, Collections.singletonList(value));
            }
        });
        ThreadLocalUtils.setRequestHeader(new RequestHeader(header));
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        ThreadLocalUtils.removeRequestHeader();
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        ThreadLocalUtils.removeRequestHeader();
        return context;
    }
}