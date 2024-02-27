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
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.router.common.handler.Handler;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.common.utils.DubboReflectUtils;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;
import com.huaweicloud.sermant.router.dubbo.handler.AbstractContextFilterHandler;
import com.huaweicloud.sermant.router.dubbo.handler.LaneContextFilterHandler;
import com.huaweicloud.sermant.router.dubbo.handler.RouteContextFilterHandler;
import com.huaweicloud.sermant.router.dubbo.service.DubboConfigService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * 增强ContextFilter类的invoke方法
 *
 * @author provenceee
 * @since 2022-09-26
 */
public class ContextFilterInterceptor extends AbstractInterceptor {
    private final List<AbstractContextFilterHandler> handlers;

    private final DubboConfigService configService;

    /**
     * 构造方法
     */
    public ContextFilterInterceptor() {
        handlers = new ArrayList<>();
        handlers.add(new LaneContextFilterHandler());
        handlers.add(new RouteContextFilterHandler());
        handlers.sort(Comparator.comparingInt(Handler::getOrder));
        configService = PluginServiceManager.getPluginService(DubboConfigService.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        Set<String> matchKeys = configService.getMatchKeys();
        Set<String> injectTags = configService.getInjectTags();
        if (CollectionUtils.isEmpty(matchKeys) && CollectionUtils.isEmpty(injectTags)) {
            // 染色标记为空，代表没有染色规则，直接return
            return context;
        }

        Object[] arguments = context.getArguments();
        handlers.forEach(handler -> ThreadLocalUtils.addRequestTag(handler.getRequestTag(
                arguments[0], arguments[1], DubboReflectUtils.getAttachments(arguments[1]), matchKeys, injectTags)));
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        ThreadLocalUtils.removeRequestTag();
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        ThreadLocalUtils.removeRequestTag();
        return context;
    }
}