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

package io.sermant.spring.feign.consumer.controller;

import io.sermant.spring.feign.api.BootRegistryService;
import io.sermant.spring.feign.api.FeignService;

import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试接口
 *
 * @author provenceee
 * @since 2022-11-02
 */
@RestController
@RequestMapping("/router")
public class RouterController {
    private static final String PROVIDER_URL = "http://feign-provider/lane";

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${service_meta_version:${SERVICE_META_VERSION:${service.meta.version:1.0.0}}}")
    private String version;

    @Autowired
    private BootRegistryService bootRegistryService;

    @Autowired
    private FeignService feignService;

    @Autowired
    @Lazy
    private WebClient client;

    /**
     * 获取区域
     *
     * @param exit 是否退出
     * @return 区域
     */
    @GetMapping("/boot/getMetadata")
    public String getMetadataByBoot(@RequestParam("exit") boolean exit) {
        return bootRegistryService.getMetadata(exit);
    }

    /**
     * 获取区域
     *
     * @param exit 是否退出
     * @return 区域
     */
    @GetMapping("/cloud/getMetadata")
    public String getMetadataByCloud(@RequestParam("exit") boolean exit) {
        return feignService.getMetadata(exit);
    }

    /**
     * 获取泳道信息
     *
     * @param name name
     * @param id id
     * @param enabled enabled
     * @return 泳道信息
     */
    @GetMapping("/cloud/getLane")
    public Map<String, Object> getLaneByCloud(@RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "id", defaultValue = "0") int id,
            @RequestParam(value = "enabled", defaultValue = "false") boolean enabled) {
        Map<String, Object> result = new HashMap<>(feignService.getLane());
        result.put(applicationName, getMetadata());
        return result;
    }

    /**
     * 获取泳道信息
     *
     * @param name name
     * @param id id
     * @param enabled enabled
     * @return 泳道信息
     */
    @GetMapping("/cloud/getLaneByWebclient")
    public Mono<Map<String, Object>> getLaneByCloudWebclient(
            @RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "id", defaultValue = "0") int id,
            @RequestParam(value = "enabled", defaultValue = "false") boolean enabled) {
        RequestHeadersSpec<?> requestHeadersSpec = client.get().uri(PROVIDER_URL);
        ResponseSpec spec = requestHeadersSpec.retrieve();
        return spec.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
        }).map(map -> {
            map.put(applicationName, getMetadata());
            return map;
        });
    }

    private Map<String, Object> getMetadata() {
        Map<String, Object> meta = new HashMap<>();
        meta.put("version", version);
        return meta;
    }
}