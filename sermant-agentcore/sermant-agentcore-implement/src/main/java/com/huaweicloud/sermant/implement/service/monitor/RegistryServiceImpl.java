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

package com.huaweicloud.sermant.implement.service.monitor;

import com.huaweicloud.sermant.core.service.monitor.RegistryService;

import com.sun.net.httpserver.HttpHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 注册信息获取
 *
 * @author zhp
 * @version 1.0.0
 * @since 2022-11-02
 */
public class RegistryServiceImpl implements RegistryService {
    private Map<String, HttpHandler> handlerMap = new HashMap<>();

    private List<Object> registryList = new ArrayList<>();

    @Override
    public Map<String, HttpHandler> getHandlers() {
        return handlerMap;
    }

    @Override
    public void addHandler(String name, HttpHandler httpHandler) {
        handlerMap.put(name, httpHandler);
    }
}
