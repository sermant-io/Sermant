/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowre.flowreplay.config;

import lombok.SneakyThrows;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;

/**
 * 配置httpclient单例
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-07-09
 */
@Configuration
public class HttpClientConfig {
    @Value("${http.timeout}")
    int timeout;

    @Value("${http.connection.manager.size}")
    int maxTotal;

    @SneakyThrows
    @Bean
    public CloseableHttpClient httpClient() {
        PoolingHttpClientConnectionManager connectionManager;
        SSLContext sslContext =
            new SSLContextBuilder().loadTrustMaterial(null, (chain, auType) -> true).build();
        SSLConnectionSocketFactory sslConnectionSocketFactory =
            new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.INSTANCE)
            .register("https", sslConnectionSocketFactory)
            .build();

        // 设置连接池
        connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

        // 设置连接池大小
        connectionManager.setMaxTotal(maxTotal);
        connectionManager.setDefaultMaxPerRoute(connectionManager.getMaxTotal());
        RequestConfig.Builder configBuilder = RequestConfig.custom();

        // 设置连接超时
        configBuilder.setConnectTimeout(timeout);

        // 设置读取超时
        configBuilder.setSocketTimeout(timeout);

        // 设置从连接池获取连接实例的超时
        configBuilder.setConnectionRequestTimeout(timeout);
        RequestConfig requestConfig = configBuilder.build();
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig)
            .setConnectionManager(connectionManager).build();
        return httpClient;
    }
}
