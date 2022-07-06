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

package com.huawei.flowcontrol.inject.retry;

import com.huawei.flowcontrol.common.config.ConfigConst;
import com.huawei.flowcontrol.common.config.FlowControlConfig;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.ClassUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 重试注入配置类
 * <p>注意：当前Resttemplate仅可采用注入方式, 注入使用@Primary注解, 用户自定义的Resttemplate将会失效,
 * 若使用@Primary则会导致服务无法启动。待后续优化</p>
 *
 * @author zhouss
 * @since 2022-07-23
 */
@Configuration
@ConditionalOnProperty(value = "sermant.flowcontrol.retry.enabled",
        havingValue = "true", matchIfMissing = true)
public class SpringRetryConfiguration {
    /**
     * 流控重试注入
     *
     * @return resttemplate
     */
    @Bean
    @LoadBalanced
    @Primary
    @ConditionalOnClass(name = "org.springframework.web.client.RestTemplate")
    public RestTemplate restTemplate(@Autowired(required = false) ClientHttpRequestFactory clientHttpRequestFactory) {
        final RetryableRestTemplate retryableRestTemplate = new RetryableRestTemplate();
        if (clientHttpRequestFactory != null) {
            retryableRestTemplate.setRequestFactory(clientHttpRequestFactory);
            return retryableRestTemplate;
        }
        final FlowControlConfig config = PluginConfigManager.getPluginConfig(FlowControlConfig.class);
        if (ConfigConst.REST_TEMPLATE_REQUEST_FACTORY_OK_HTTP.equals(config.getRestTemplateRequestFactory())) {
            if (ClassUtils.loadClass("okhttp3.OkHttpClient", Thread.currentThread().getContextClassLoader())
                    .isPresent()) {
                final OkHttp3ClientHttpRequestFactory okHttp3ClientHttpRequestFactory =
                        new OkHttp3ClientHttpRequestFactory();
                okHttp3ClientHttpRequestFactory.setConnectTimeout((int) config.getRestTemplateConnectTimeoutMs());
                okHttp3ClientHttpRequestFactory.setReadTimeout((int) config.getRestTemplateReadTimeoutMs());
                retryableRestTemplate.setRequestFactory(okHttp3ClientHttpRequestFactory);
            }
        } else if (ConfigConst.REST_TEMPLATE_REQUEST_FACTORY_NETTY.equals(config.getRestTemplateRequestFactory())) {
            if (ClassUtils.loadClass("io.netty.channel.nio.NioEventLoopGroup",
                    Thread.currentThread().getContextClassLoader()).isPresent()) {
                final Netty4ClientHttpRequestFactory netty4ClientHttpRequestFactory =
                        new Netty4ClientHttpRequestFactory();
                netty4ClientHttpRequestFactory.setConnectTimeout((int) config.getRestTemplateConnectTimeoutMs());
                netty4ClientHttpRequestFactory.setReadTimeout((int) config.getRestTemplateReadTimeoutMs());
                retryableRestTemplate.setRequestFactory(netty4ClientHttpRequestFactory);
            }
        }
        return retryableRestTemplate;
    }
}
