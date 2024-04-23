/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.spring.feign.api.configuration;

import feign.Client;
import feign.Feign;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerProperties;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLSocketFactory;

/**
 * 针对springCloud 2020.0.0/2021.0.0/2021.0.3高版本FeignClient SSL请求证书认证处理
 *
 * @author chengyouling
 * @since 2023-02-10
 */
@Configuration
public class FeignClientSslConfigurationHighVersion {
    private static SSLSocketFactory feignSocketFactory = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(FeignClientSslConfigurationHighVersion.class);

    /**
     * 构建Feign Builder
     *
     * @param loadBalancerClient loadBalancerClient
     * @param properties balancerProperties
     * @param factory clientFactory
     * @return Client
     */
    @Bean
    public Feign.Builder feignBuilder(LoadBalancerClient loadBalancerClient, LoadBalancerProperties properties,
            LoadBalancerClientFactory factory) {
        final Client sslClient = feignClient(loadBalancerClient, properties, factory);
        return Feign.builder().client(sslClient);
    }

    /**
     * 构建Feign client
     *
     * @param loadBalancerClient loadBalancerClient
     * @param properties balancerProperties
     * @param factory clientFactory
     * @return Client
     */
    @Bean
    public Client feignClient(LoadBalancerClient loadBalancerClient, LoadBalancerProperties properties,
            LoadBalancerClientFactory factory) {
        if (feignSocketFactory == null) {
            try {
                feignSocketFactory = FeignClientConfigSslUtils.getFeignSslSocketFactory();
            } catch (NoSuchAlgorithmException e) {
                LOGGER.error("build ssl feign client failed for NoSuchAlgorithmException");
            } catch (KeyManagementException e) {
                LOGGER.error("build ssl feign client failed for KeyManagementException");
            }
        }
        return new FeignBlockingLoadBalancerClient(new Client.Default(feignSocketFactory, new NoopHostnameVerifier()),
                loadBalancerClient, properties, factory);
    }
}
