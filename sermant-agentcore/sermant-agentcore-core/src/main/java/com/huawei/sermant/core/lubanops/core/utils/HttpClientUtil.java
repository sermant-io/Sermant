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

package com.huawei.sermant.core.lubanops.core.utils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

import com.huawei.sermant.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.sermant.core.lubanops.integration.enums.HttpMethod;
import com.huawei.sermant.core.lubanops.integration.transport.http.HttpRequest;
import com.huawei.sermant.core.lubanops.integration.transport.http.HttpSigner;

/**
 * 发送http请求的基类
 * @author
 */
public class HttpClientUtil {
    public static final PoolingHttpClientConnectionManager CONN_MANAGER;

    public static final CloseableHttpClient HTTP_CLIENT;

    public static final HttpSigner HTTP_SIGNER;

    private final static Logger LOGGER = LogFactory.getLogger();

    private static SSLContext sslContext = null;

    static {

        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(new TrustStrategy() {

                @Override
                public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

                    return true;
                }
            }).build();
        } catch (KeyManagementException e) {
            throw new RuntimeException("failed to get SSLContext", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("failed to get SSLContext", e);
        } catch (KeyStoreException e) {
            throw new RuntimeException("failed to get SSLContext", e);
        }

        HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http",
                        PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslsf).build();
        CONN_MANAGER = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

        CONN_MANAGER.setDefaultMaxPerRoute(1);
        CONN_MANAGER.setMaxTotal(1);
        CONN_MANAGER.closeIdleConnections(1, TimeUnit.MILLISECONDS);
        HTTP_CLIENT = HttpClients.custom()
                .setConnectionManager(CONN_MANAGER)
                .setConnectionReuseStrategy(new ConnectionReuseStrategy() {
                    @Override
                    public boolean keepAlive(HttpResponse response, HttpContext context) {
                        return false;
                    }
                })
                .setRetryHandler(new DefaultHttpRequestRetryHandler())
                .build();
        HTTP_SIGNER = new HttpSigner();

    }

    public static Result sendPostJSON(String url, String encoding, String content, int timeout) throws Exception {
        // 设置请求和传输超时时间
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(timeout)
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .build();
        HttpRequest request = buildSignedRequest(url, content);
        HttpPost httpPost = (HttpPost) request.generate();
        httpPost.setConfig(requestConfig);
        HttpEntity entity = null;
        CloseableHttpResponse closeableHttpResponse = null;
        try {
            closeableHttpResponse = HTTP_CLIENT.execute(httpPost);
            StatusLine st = closeableHttpResponse.getStatusLine();
            entity = closeableHttpResponse.getEntity();
            String resultStr = EntityUtils.toString(entity, encoding);
            return new Result(st.getStatusCode(), resultStr);
        } finally {
            if (entity != null) {
                try {
                    EntityUtils.consume(entity);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "", e);
                }
            }
            if (closeableHttpResponse != null) {
                try {
                    closeableHttpResponse.close();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "", e);
                }
            }
        }
    }

    public static Result sendPostJSONProxy(String url, String encoding, String contentw, int timeout, String proxy)
            throws Exception {
        // 设置请求和传输超时时间
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(timeout)
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .build();
        HttpRequest request = new HttpRequest();
        request.setMethod(HttpMethod.POST.name());
        request.setUrl(url);
        request.addHeader("Content-type", "application/json; charset=utf-8");
        request.setBody(contentw);
        HTTP_SIGNER.sign(request);
        HttpPost httpPost = (HttpPost) request.generate();
        httpPost.setConfig(requestConfig);
        HttpEntity proxyentity = null;
        CloseableHttpResponse httpResponse = null;
        try {
            HttpClientContext context = HttpClientContext.create();
            context.setAttribute("proxy", proxy);
            httpResponse = HTTP_CLIENT.execute(httpPost, context);
            StatusLine st = httpResponse.getStatusLine();
            proxyentity = httpResponse.getEntity();
            String resultstring = EntityUtils.toString(proxyentity, encoding);
            return new Result(st.getStatusCode(), resultstring);
        } finally {
            if (proxyentity != null) {
                try {
                    EntityUtils.consume(proxyentity);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "", e);
                }
            }
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "", e);
                }
            }
        }
    }

    private static HttpRequest buildSignedRequest(String url, String content) throws Exception {
        HttpRequest request = new HttpRequest();
        request.setMethod(HttpMethod.POST.name());
        request.setUrl(url);
        request.addHeader("Content-type", "application/json; charset=utf-8");
        request.setBody(content);
        HTTP_SIGNER.sign(request);
        return request;
    }

    /**
     * 发送消息 <br>
     * @return
     * @author
     * @since 2020年3月16日
     */
    public static Result sendPostToServer(List<String> addressList, String[] proxyList, String url, String params) {
        Collections.shuffle(addressList);
        Exception exception = null;
        Result response = null;
        for (String masterAddress : addressList) {
            if (proxyList != null && proxyList.length > 0) {
                List<String> proxyIpList = Arrays.asList(proxyList);
                Collections.shuffle(proxyIpList);
                for (String proxy : proxyIpList) {
                    try {
                        response = HttpClientUtil.sendPostJSONProxy(masterAddress + url, "utf-8", params, 3000, proxy);

                        if (response.getStatus() == 200) {
                            return response;
                        }
                    } catch (Exception e) {
                        exception = e;
                    }
                }
            } else {
                try {
                    response = HttpClientUtil.sendPostJSON(masterAddress + url, "utf-8", params, 3000);
                    if (response.getStatus() == 200) {
                        return response;
                    }
                } catch (Exception e) {
                    exception = e;
                }
            }
        }

        if (response != null) {
            return response;
        } else if (exception != null) {
            LOGGER.log(Level.SEVERE, url + " error", exception);
        }
        return response;
    }

    public static class Result {
        private int status;

        private String content;

        Result(int status, String content) {
            this.status = status;
            this.content = content;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return "[" + status + "][" + content + "]";
        }

    }

}
