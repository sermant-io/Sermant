/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.spring.handler;

import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.router.spring.service.SpringConfigService;

import java.util.List;
import java.util.Map;

/**
 * AbstractHandlerMapping processor
 *
 * @author provenceee
 * @since 2023-02-21
 */
public abstract class AbstractMappingHandler extends AbstractHandler {
    /**
     * Configuration service
     */
    protected final SpringConfigService configService;

    /**
     * Constructor
     */
    public AbstractMappingHandler() {
        configService = PluginServiceManager.getPluginService(SpringConfigService.class);
    }

    /**
     * Obtain transparent tags
     *
     * @param path The path of the request
     * @param methodName http method
     * @param headers HTTP request headers
     * @param parameters URL parameter
     * @return Marks for transparent transmission
     */
    public abstract Map<String, List<String>> getRequestTag(String path, String methodName,
            Map<String, List<String>> headers, Map<String, List<String>> parameters);
}