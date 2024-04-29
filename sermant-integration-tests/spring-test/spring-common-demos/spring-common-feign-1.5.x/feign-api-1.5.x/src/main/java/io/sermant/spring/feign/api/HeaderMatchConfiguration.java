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

package io.sermant.spring.feign.api;

import feign.Client;
import feign.Feign;
import feign.RequestInterceptor;
import feign.RequestTemplate;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.feign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.netflix.feign.ribbon.LoadBalancerFeignClient;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.annotation.Bean;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * 针对header方法增加请求头判断是否可以匹配成功
 *
 * @author zhouss
 * @since 2022-07-29
 */
public class HeaderMatchConfiguration implements RequestInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeaderMatchConfiguration.class);
    private static final String KEY = "key";
    private static SSLSocketFactory feignSocketFactory = null;

    @Override
    public void apply(RequestTemplate template) {
        final String url = template.url();
        if (url.contains("headerExact")) {
            template.header(KEY, "flowControlExact");
        } else if (url.contains("headerPrefix")) {
            template.header(KEY, "flowControlPrefix");
        } else if (url.contains("headerSuffix")) {
            template.header(KEY, "flowControlSuffix");
        } else if (url.contains("headerContains")) {
            template.header(KEY, "flowControlContains");
        } else if (url.contains("headerCompareMatch")) {
            template.header(KEY, "102");
        } else if (url.contains("headerCompareNotMatch")) {
            template.header(KEY, "100");
        }
    }

    /**
     * 构建Feign Builder
     *
     * @param lbClientFactory LB工厂
     * @param clientFactory client工厂
     * @return Feign.Builder
     */
    @Bean
    public Feign.Builder feignBuilder(CachingSpringLoadBalancerFactory lbClientFactory,
            SpringClientFactory clientFactory) {
        final Client sslClient = client(lbClientFactory, clientFactory);
        return Feign.builder().client(sslClient);
    }

    /**
     * 构建Feign client
     *
     * @param lbClientFactory LB工厂
     * @param clientFactory client工厂
     * @return client
     */
    @Bean
    public Client client(CachingSpringLoadBalancerFactory lbClientFactory, SpringClientFactory clientFactory) {
        if (feignSocketFactory == null) {
            try {
                feignSocketFactory = getFeignSslSocketFactory();
            } catch (NoSuchAlgorithmException e) {
                LOGGER.error("build ssl feign client failed for NoSuchAlgorithmException");
            } catch (KeyManagementException e) {
                LOGGER.error("build ssl feign client failed for KeyManagementException");
            }
        }
        return new LoadBalancerFeignClient(new Client.Default(feignSocketFactory, new NoopHostnameVerifier()),
                lbClientFactory, clientFactory);
    }

    private SSLSocketFactory getFeignSslSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustManagers = new TrustManager[1];
        TrustManager tm = new SslTrustManager();
        trustManagers[0] = tm;
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustManagers, null);
        return sslContext.getSocketFactory();
    }

    /**
     * 构建SSL Manager
     *
     * @since 2022-07-29
     */
    static class SslTrustManager implements TrustManager, X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
