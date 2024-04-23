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

import io.sermant.spring.common.registry.common.RegistryConstants;
import io.sermant.spring.feign.api.BootRegistryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

/**
 * 注册控制器
 *
 * @author zhouss
 * @since 2022-10-25
 */
@Controller
@ResponseBody
@RequestMapping(RegistryConstants.REGISTRY_REQUEST_PREFIX)
public class BootRegistryController {
    @Autowired
    private BootRegistryService bootRegistryService;

    /**
     * 测试feign调用
     *
     * @return ok
     */
    @RequestMapping("feignRegistry")
    public String feignRegistry() {
        return bootRegistryService.feignRegistry();
    }

    /**
     * 测试feign调用
     *
     * @return ok
     */
    @RequestMapping("feignRegistryPost")
    public String feignRegistryPost() {
        return bootRegistryService.feignRegistryPost();
    }

    /**
     * 测试webclient调用
     *
     * @param type 客户端类型：httpClient/jetty/reactor
     * @return ok
     */
    @RequestMapping("webClientRegistry")
    public String webClientRegistry(@RequestParam(value = "type", defaultValue = "reactor") String type) {
        WebClient client = getWebClient(type);
        RequestHeadersSpec<?> requestHeadersSpec = client.get()
                .uri("http://" + RegistryConstants.TEST_DOMAIN + "/feign-provider/feignRegistry");
        ResponseSpec spec = requestHeadersSpec.retrieve();
        return spec.bodyToMono(String.class).block();
    }

    /**
     * 测试webclient调用
     *
     * @param type 客户端类型：httpClient/jetty/reactor
     * @return ok
     */
    @RequestMapping("webClientRegistryPost")
    public String webClientRegistryPost(@RequestParam(value = "type", defaultValue = "reactor") String type) {
        WebClient client = getWebClient(type);
        RequestHeadersSpec<?> requestHeadersSpec = client.post()
                .uri("http://" + RegistryConstants.TEST_DOMAIN + "/feign-provider/feignRegistryPost");
        ResponseSpec spec = requestHeadersSpec.retrieve();
        return spec.bodyToMono(String.class).block();
    }

    private WebClient getWebClient(String type) {
        try {
            if ("httpClient".equals(type)) {
                return WebClient.builder().clientConnector((ClientHttpConnector) Class
                        .forName("org.springframework.http.client.reactive.HttpComponentsClientHttpConnector")
                        .newInstance()).build();
            }
            if ("jetty".equals(type)) {
                return WebClient.builder().clientConnector((ClientHttpConnector) Class
                        .forName("org.springframework.http.client.reactive.JettyClientHttpConnector")
                        .newInstance()).build();
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ignored) {
            // 版本原因有可能找不到类，使用默认clientConnector
        }
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector()).build();
    }
}