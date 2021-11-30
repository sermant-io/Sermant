/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.flowreplay.datasource;

import com.huawei.flowre.flowreplay.config.Const;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Es的客户端
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-03-26
 */
@Component
public class EsClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(EsClient.class);

    @Value("${elasticsearch.host}")
    private String esHostName;

    @Value("${http.timeout}")
    private int timeOut;

    @Value("${elasticsearch.username}")
    private String userName;

    @Value("${elasticsearch.password}")
    private String passwd;

    @Bean(destroyMethod = "close")
    public RestHighLevelClient initEsClient() {
        String[] split = esHostName.split(Const.COMMA);
        HttpHost[] hostsArray = new HttpHost[split.length];
        for (int i = 0; i < split.length; i++) {
            String item = split[i];
            hostsArray[i] = new HttpHost(item.split(Const.COLON)[0], Integer.parseInt(item.split(Const.COLON)[1]),
                    "http");
        }

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, passwd));
        return new RestHighLevelClient(
                RestClient.builder(hostsArray).setHttpClientConfigCallback(httpClientBuilder -> {
                    RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
                            .setConnectionRequestTimeout(timeOut)
                            .setSocketTimeout(timeOut)
                            .setConnectionRequestTimeout(timeOut);
                    httpClientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    return httpClientBuilder;
                })
        );
    }
}
