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

package com.huawei.nacos.rest.consumer;

import com.huawei.nacos.common.HostUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 优雅上下线请求
 *
 * @author zhouss
 * @since 2022-06-16
 */
@RestController
public class GraceController {
    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${grace.openProviderName}")
    private String openProviderServiceName;

    @Value("${grace.closeProviderName}")
    private String closeProviderServiceName;

    @Autowired
    private RestTemplate restTemplate;

    private String ip;

    /**
     * 开启优雅下线请求
     *
     * @param request 请求信息
     * @param response 响应信息
     * @return 结果
     */
    @RequestMapping("/graceDownOpen")
    public String graceDownOpen(HttpServletRequest request, HttpServletResponse response) {
        return graceDownTest(request, openProviderServiceName, response);
    }

    /**
     * 服务预热
     *
     * @param request 请求信息
     * @param response 响应信息
     * @return 结果
     */
    @RequestMapping("/graceHot")
    public String graceHot(HttpServletRequest request, HttpServletResponse response) {
        return graceDownTest(request, openProviderServiceName, response);
    }

    /**
     * 关闭优雅下线请求
     *
     * @param request 请求信息
     * @param response 响应信息
     * @return 结果
     */
    @RequestMapping("/graceDownClose")
    public String graceDownClose(HttpServletRequest request, HttpServletResponse response) {
        return graceDownTest(request, closeProviderServiceName, response);
    }

    private String graceDownTest(HttpServletRequest request, String downServiceName, HttpServletResponse response) {
        final String result = restTemplate
                .execute(String.format(Locale.ENGLISH, "http://%s/graceDown", downServiceName), HttpMethod.GET,
                        restTemplate.acceptHeaderRequestCallback(String.class),
                        new MyResponseExtractor(String.class, restTemplate.getMessageConverters(), response));
        if (ip == null) {
            ip = HostUtils.getMachineIp();
        }
        final String cur = String
                .format(Locale.ENGLISH, "%s[%s:%s]", serviceName, ip, request.getLocalPort());
        return cur + "->" + result;
    }

    /**
     * 响应执行器
     *
     * @since 2022-06-22
     */
    static class MyResponseExtractor extends HttpMessageConverterExtractor<String> {
        private final HttpServletResponse response;

        MyResponseExtractor(Class<String> responseType,
                List<HttpMessageConverter<?>> messageConverters, HttpServletResponse response) {
            super(responseType, messageConverters);
            this.response = response;
        }

        @Override
        public String extractData(ClientHttpResponse clientHttpResponse) throws IOException {
            final String result = super.extractData(clientHttpResponse);
            final HttpHeaders headers = clientHttpResponse.getHeaders();
            final Set<Entry<String, List<String>>> entries = headers.entrySet();
            for (Entry<String, List<String>> entry : entries) {
                response.addHeader(entry.getKey(), entry.getValue().isEmpty() ? null
                        : entry.getValue().get(0));
            }
            return result;
        }
    }
}
