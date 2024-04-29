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

package io.sermant.router.dubbo.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.core.plugin.service.PluginServiceManager;
import io.sermant.router.common.handler.Handler;
import io.sermant.router.common.utils.CollectionUtils;
import io.sermant.router.common.utils.DubboReflectUtils;
import io.sermant.router.common.utils.ThreadLocalUtils;
import io.sermant.router.dubbo.handler.AbstractContextFilterHandler;
import io.sermant.router.dubbo.handler.LaneContextFilterHandler;
import io.sermant.router.dubbo.handler.RouteContextFilterHandler;
import io.sermant.router.dubbo.service.DubboConfigService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Enhance the Invoke Method of ContextFilter Class
 *
 * @author provenceee
 * @since 2022-09-26
 */
public class ContextFilterInterceptor extends AbstractInterceptor {
    private final List<AbstractContextFilterHandler> handlers;

    private final DubboConfigService configService;

    /**
     * Constructor
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
            // The staining mark is empty, which means that there are no staining rules, and it is returned directly
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