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

package com.huaweicloud.spring.common.flowcontrol.consumer;

import com.huaweicloud.spring.common.flowcontrol.handlers.RestTemplateResponseErrorHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Locale;

import javax.annotation.PostConstruct;

/**
 * 流控服务端测试
 *
 * @author zhouss
 * @since 2022-07-28
 */
@Controller
@ResponseBody
@RequestMapping("/flowcontrol")
public class ServerController {
    @Value("${down.serviceName}")
    private String downServiceName;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        restTemplate.setErrorHandler(new RestTemplateResponseErrorHandler());
    }

    /**
     * 限流测试
     *
     * @return ok
     */
    @RequestMapping("rateLimiting")
    public String rateLimiting() {
        return restTemplate.getForObject(buildUrl("rateLimiting"), String.class);
    }

    /**
     * 慢调用熔断测试
     *
     * @return ok
     */
    @RequestMapping("timedBreaker")
    public String timedBreaker() {
        return restTemplate.getForObject(buildUrl("timedBreaker"), String.class);
    }

    /**
     * 异常熔断测试
     *
     * @return ok
     */
    @RequestMapping("exceptionBreaker")
    public String exceptionBreaker() {
        return restTemplate.getForObject(buildUrl("exceptionBreaker"), String.class);
    }

    /**
     * 隔离仓测试
     *
     * @return ok
     */
    @RequestMapping("bulkhead")
    public String bulkhead() {
        return restTemplate.getForObject(buildUrl("bulkhead"), String.class);
    }

    /**
     * 请求头匹配测试
     *
     * @return ok
     */
    @RequestMapping("header")
    public String header() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("key", "header1");
        headers.set("contentType", "application/json;charset=UTF-8");
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity(null, headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(buildUrl("header"));
        return restTemplate.exchange(builder.build().toString(), HttpMethod.GET, request, String.class).getBody();
    }

    /**
     * 匹配服务名测试-匹配前提, 触发流控
     *
     * @return ok
     */
    @RequestMapping("serviceNameMatch")
    public String serviceNameMatch() {
        return restTemplate.getForObject(buildUrl("serviceNameMatch"), String.class);
    }

    /**
     * 匹配服务名测试-不匹配前提, 不触发流控
     *
     * @return ok
     */
    @RequestMapping("serviceNameNoMatch")
    public String serviceNameNoMatch() {
        return restTemplate.getForObject(buildUrl("serviceNameNoMatch"), String.class);
    }

    private String buildUrl(String api) {
        return String.format(Locale.ENGLISH, "http://%s/%s", downServiceName, api);
    }
}
