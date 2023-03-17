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

package com.huaweicloud.integration.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * ConsumerController
 *
 * @author provenceee
 * @since 2022-07-26
 */
@RestController
public class ProviderController {
    @Value("${server.port}")
    private int port;

    @Value("${service_meta_version:${SERVICE_META_VERSION:${service.meta.version:1.0.0}}}")
    private String version;

    @Value("${service_meta_parameters:${SERVICE_META_PARAMETERS:${service.meta.parameters:}}}")
    private String parameters;

    @Value("${spring.application.name}")
    private String name;

    @Value("${service_meta_zone:${SERVICE_META_ZONE:${service.meta.zone:bar}}}")
    private String zone;

    /**
     * 测试方法
     *
     * @return msg
     */
    @GetMapping("hello")
    public String hello() {
        return "Hello world! I'm " + name + ", my port is " + port + ", my version is " + version + ", my zone is "
                + zone + ", my parameters is [" + parameters + "].";
    }

    /**
     * 获取泳道信息
     *
     * @return msg
     */
    @GetMapping("lane")
    public Map<String, Object> getLane() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        Map<String, String> map = new HashMap<>();
        Enumeration<?> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            map.put(key, enumeration2List(request.getHeaders(key)).get(0));
        }
        map.put("version", version);
        Map<String, Object> result = new HashMap<>();
        result.put(name, map);
        return result;
    }

    private List<String> enumeration2List(Enumeration<?> enumeration) {
        List<String> collection = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            collection.add((String) enumeration.nextElement());
        }
        return collection;
    }
}