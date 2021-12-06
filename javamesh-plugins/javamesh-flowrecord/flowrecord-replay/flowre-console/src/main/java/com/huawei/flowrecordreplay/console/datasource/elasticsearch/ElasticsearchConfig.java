/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.flowrecordreplay.console.datasource.elasticsearch;

import com.huawei.flowrecordreplay.console.util.Constant;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * elasticsearch连接配置
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-02-26
 */
@Configuration
public class ElasticsearchConfig {
    /**
     * 连接超时时间
     */
    private static final int CONNECT_TIMEOUT_MS = 5000;

    /**
     * 请求获取数据超时时间
     */
    private static final int SOCKET_TIMEOUT_MS = 5000;

    /**
     * 从连接池获取Connection超时时间
     */
    private static final int CONNECTION_REQUEST_TIMEOUT_MS = 5000;

    @Value("${elasticsearch.hostList}")
    private String hostList;

    @Value("${elasticsearch.userName}")
    private String userName;

    @Value("${elasticsearch.passwd}")
    private String passwd;

    @Bean(destroyMethod = "close")
    @Scope("prototype")
    public RestHighLevelClient restHighLevelClient() {
        String[] split = hostList.split(Constant.COMMA);
        HttpHost[] hostsArray = new HttpHost[split.length];
        for (int i = 0; i < split.length; i++) {
            String item = split[i];
            hostsArray[i] = new HttpHost(item.split(Constant.COLON)[0], Integer.parseInt(item.split(Constant.COLON)[1]),
                    "http");
        }

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, passwd));
        RestClientBuilder builder = RestClient.builder(hostsArray)
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
                            .setConnectTimeout(CONNECT_TIMEOUT_MS)
                            .setSocketTimeout(SOCKET_TIMEOUT_MS)
                            .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_MS);
                    httpClientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    return httpClientBuilder;
                });
        return new RestHighLevelClient(builder);
    }
}
