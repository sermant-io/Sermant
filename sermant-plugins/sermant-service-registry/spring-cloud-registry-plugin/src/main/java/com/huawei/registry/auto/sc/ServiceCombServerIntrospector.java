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

package com.huawei.registry.auto.sc;

import com.netflix.loadbalancer.Server;

import org.springframework.cloud.netflix.ribbon.DefaultServerIntrospector;

import java.util.Map;

/**
 * meta处理
 *
 * @author zhouss
 * @since 2022-05-19
 */
public class ServiceCombServerIntrospector extends DefaultServerIntrospector {
    @Override
    public Map<String, String> getMetadata(Server server) {
        if (server instanceof ServiceCombServer) {
            return ((ServiceCombServer) server).getMetadata();
        }
        return super.getMetadata(server);
    }
}
