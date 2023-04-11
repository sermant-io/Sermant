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

package com.huaweicloud.spring.rest.consumer;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

/**
 * 流控配置类
 *
 * @author zhouss
 * @since 2022-07-28
 */
@Configuration
public class FlowcontrolConiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlowcontrolConiguration.class);
    private static final int TIME_OUT = 5 * 60 * 1000;

    @Value("${timeout}")
    private int timeout;

    /**
     * 注入请求器
     *
     * @return RestTemplate
     */
    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * 注入请求器
     *
     * @return RestTemplate
     */
    @LoadBalanced
    @Bean("removalRestTemplate")
    public RestTemplate removalRestTemplate() {
        RestTemplate template = new RestTemplate();
        SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory) template.getRequestFactory();
        rf.setReadTimeout(timeout);
        return template;
    }

    /**
     * 注入请求器
     *
     * @return RestTemplate
     */
    @LoadBalanced
    @Bean("routerRestTemplate")
    public RestTemplate routerRestTemplate() {
        return new RestTemplate();
    }

    /**
     * 注入请求器
     *
     * @return RestTemplate
     */
    @LoadBalanced
    @Bean("gracefulRestTemplate")
    public RestTemplate gracefulRestTemplate() {
        return buildRestTemplate();
    }

    private RestTemplate buildRestTemplate() {
        RestTemplate restTemplate = null;
        try {
            restTemplate = new RestTemplate(buildHttpRequestFactory());
        } catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            LOGGER.error("build SSL restTemplate failed!");
        }
        return restTemplate;
    }

    private HttpComponentsClientHttpRequestFactory buildHttpRequestFactory()
            throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        TrustStrategy strategy = (x509Certificates, authType) -> true;
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, strategy).build();
        SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(TIME_OUT).setConnectTimeout(TIME_OUT).setConnectionRequestTimeout(TIME_OUT).build();
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        httpClientBuilder.setDefaultRequestConfig(requestConfig);
        httpClientBuilder.setSSLSocketFactory(factory);
        CloseableHttpClient httpClient = httpClientBuilder.build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        return requestFactory;
    }
}
