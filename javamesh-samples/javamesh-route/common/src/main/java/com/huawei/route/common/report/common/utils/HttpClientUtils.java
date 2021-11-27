/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.common.report.common.utils;

import com.huawei.apm.core.common.LoggerFactory;
import com.huawei.route.common.report.common.entity.HttpClientResult;
import org.apache.http.Consts;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Description: httpClient工具类
 *
 * @author 30009881
 * @since 2021-06-15
 */
public class HttpClientUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 编码格式。发送编码格式统一用UTF-8
     */
    private static final String ENCODING = "UTF-8";

    /**
     * 设置连接超时时间，单位毫秒。指三次握手的超时时间
     */
    private static final int CONNECT_TIMEOUT = 10000;

    /**
     * 请求获取数据的超时时间(即响应时间)，单位毫秒。
     */
    private static final int SOCKET_TIMEOUT = 10000;

    private static final int MAX_TOTAL_POOL = 200;

    private static final int MAX_TIMEOUT = 8000;

    private static final int REQUEST_TIMEOUT = 5000;

    private static CloseableHttpClient client;

    private static final RequestConfig DEFAULT_REQUEST_CONFIG;

    static {
        createClient();
        DEFAULT_REQUEST_CONFIG = RequestConfig.custom()
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .build();
    }

    private static synchronized void createClient() {
        if (client != null) {
            return;
        }
        try {
            // 采用绕过验证的方式处理https请求
            SSLContext sslcontext = createIgnoreVerifySSL();

            // 设置协议http和https对应的处理socket链接工厂的对象
            Registry<ConnectionSocketFactory> socketFactoryRegistry
                    = RegistryBuilder.<ConnectionSocketFactory>create().register("http",
                    PlainConnectionSocketFactory.INSTANCE)
                    .register("https", new SSLConnectionSocketFactory(sslcontext))
                    .build();
            PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(
                    socketFactoryRegistry);
            connManager.setMaxTotal(MAX_TOTAL_POOL);
            connManager.setDefaultMaxPerRoute(MAX_TOTAL_POOL);
            RequestConfig.Builder configBuilder = RequestConfig.custom();

            // 设置连接超时
            configBuilder.setConnectTimeout(MAX_TIMEOUT);

            // 设置读取超时
            configBuilder.setSocketTimeout(MAX_TIMEOUT);

            // 设置从连接池获取连接实例的超时
            configBuilder.setConnectionRequestTimeout(REQUEST_TIMEOUT);
            IdleConnectionMonitorThread idleConnectionMonitorThread = new IdleConnectionMonitorThread(connManager);
            idleConnectionMonitorThread.start();

            // 创建自定义的httpclient对象
            client = HttpClients.custom()
                    .setConnectionManager(connManager)
                    .setConnectionManagerShared(true)
                    .setDefaultRequestConfig(configBuilder.build())
                    .build();
            LOGGER.info("PoolingHttpClientConnectionManager init success.");
        } catch (Exception e) {
            LOGGER.warning("Failed to init httpclient.");
        }
    }

    private HttpClientUtils() {
    }

    /**
     * 发送get请求；不带请求头和请求参数
     *
     * @param url 请求地址
     * @return HttpClientResult结果包装类
     * @throws URISyntaxException URI异常
     * @throws IOException        请求异常抛出
     */
    public static HttpClientResult doGet(String url) throws URISyntaxException, IOException {
        return doGet(url, null, null);
    }

    /**
     * 发送get请求；带请求参数
     *
     * @param url    请求地址
     * @param params 请求参数集合
     * @return HttpClientResult结果包装类
     * @throws URISyntaxException URI异常
     * @throws IOException        请求异常抛出
     */
    public static HttpClientResult doGet(String url, Map<String, String> params)
            throws URISyntaxException, IOException {
        return doGet(url, null, params);
    }

    /**
     * 发送get请求；带请求头和请求参数
     *
     * @param url     请求地址
     * @param headers 请求头集合
     * @param params  请求参数集合
     * @return HttpClientResult结果包装类
     * @throws URISyntaxException URI异常
     * @throws IOException        请求异常抛出
     */
    public static HttpClientResult doGet(String url, Map<String, String> headers, Map<String, String> params)
            throws URISyntaxException, IOException {
        // 创建httpClient对象
        CloseableHttpClient httpClient = getClient();

        // 创建访问的地址
        URIBuilder uriBuilder = new URIBuilder(url);
        if (params != null) {
            Set<Entry<String, String>> entrySet = params.entrySet();
            for (Entry<String, String> entry : entrySet) {
                uriBuilder.setParameter(entry.getKey(), entry.getValue());
            }
        }

        // 创建http对象
        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.setConfig(DEFAULT_REQUEST_CONFIG);

        // 设置请求头
        packageHeader(headers, httpGet);

        // 创建httpResponse对象
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);

        try {
            // 执行请求并获得响应结果
            return getHttpClientResult(httpResponse);
        } finally {
            // 释放资源
            release(httpResponse);
        }
    }

    /**
     * 发送post请求；不带请求头和请求参数
     *
     * @param url 请求地址
     * @return HttpClientResult结果包装类
     * @throws IOException 请求异常抛出
     */
    public static HttpClientResult doPost(String url) throws IOException {
        return doPost(url, null, null);
    }

    /**
     * 发送post请求；带请求参数
     *
     * @param url    请求地址
     * @param params 参数集合
     * @return HttpClientResult结果包装类
     * @throws IOException 请求异常抛出
     */
    public static HttpClientResult doPost(String url, Map<String, String> params) throws IOException {
        return doPost(url, null, params);
    }

    /**
     * 发送post请求；带请求头和请求参数
     *
     * @param url     请求地址
     * @param headers 请求头集合
     * @param params  请求参数集合
     * @return HttpClientResult结果包装类
     * @throws IOException 请求异常抛出
     */
    public static HttpClientResult doPost(String url, Map<String, String> headers, Map<String, String> params)
            throws IOException {
        // 创建httpClient对象
        CloseableHttpClient httpClient = getClient();

        // 创建http对象
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(DEFAULT_REQUEST_CONFIG);

        // 设置请求头
        packageHeader(headers, httpPost);

        // 封装请求参数
        packageParam(params, httpPost);

        // 创建httpResponse对象
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);

        try {
            // 执行请求并获得响应结果
            return getHttpClientResult(httpResponse);
        } finally {
            // 释放资源
            release(httpResponse);
        }
    }

    /**
     * 发送post请求；请求参数为json
     *
     * @param url  请求地址
     * @param json json字符串
     * @return HttpClientResult结果包装类
     * @throws IOException 请求异常抛出
     */
    public static HttpClientResult doPost(String url, String json) throws IOException {
        // 创建httpClient对象
        CloseableHttpClient httpClient = getClient();

        // 创建http对象
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(DEFAULT_REQUEST_CONFIG);
        StringEntity stringEntity = new StringEntity(json, Consts.UTF_8);
        stringEntity.setContentType(new BasicHeader("Content-Type", "application/json;charset=utf-8"));
        stringEntity.setContentEncoding(Consts.UTF_8.name());
        httpPost.setEntity(stringEntity);
        // 创建httpResponse对象
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        try {
            // 执行请求并获得响应结果
            return getHttpClientResult(httpResponse);
        } finally {
            // 释放资源
            release(httpResponse);
        }
    }

    /**
     * 发送put请求；不带请求参数
     *
     * @param url 请求地址
     * @return HttpClientResult结果包装类
     */
    public static HttpClientResult doPut(String url) throws IOException {
        return doPut(url, Collections.<String, String>emptyMap());
    }

    /**
     * 发送put请求；带请求参数
     *
     * @param url    请求地址
     * @param params 参数集合
     * @return HttpClientResult结果包装类
     * @throws IOException 请求异常抛出
     */
    public static HttpClientResult doPut(String url, Map<String, String> params) throws IOException {
        CloseableHttpClient httpClient = getClient();
        HttpPut httpPut = new HttpPut(url);
        httpPut.setConfig(DEFAULT_REQUEST_CONFIG);

        packageParam(params, httpPut);

        CloseableHttpResponse httpResponse = httpClient.execute(httpPut);

        try {
            return getHttpClientResult(httpResponse);
        } finally {
            release(httpResponse);
        }
    }

    /**
     * 发送delete请求；不带请求参数
     *
     * @param url 请求地址
     * @return HttpClientResult结果包装类
     * @throws IOException 请求异常抛出
     */
    public static HttpClientResult doDelete(String url) throws IOException {
        CloseableHttpClient httpClient = getClient();
        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.setConfig(DEFAULT_REQUEST_CONFIG);

        CloseableHttpResponse httpResponse = httpClient.execute(httpDelete);
        try {
            return getHttpClientResult(httpResponse);
        } finally {
            release(httpResponse);
        }
    }

    /**
     * Description: 封装请求头
     *
     * @param headers    请求headers
     * @param httpMethod 请求方法
     */
    private static void packageHeader(Map<String, String> headers, HttpRequestBase httpMethod) {
        // 封装请求头
        if (headers != null) {
            Set<Entry<String, String>> entrySet = headers.entrySet();
            for (Entry<String, String> entry : entrySet) {
                // 设置到请求头到HttpRequestBase对象中
                httpMethod.setHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Description: 封装请求参数
     *
     * @param params     参数
     * @param httpMethod 请求方法
     * @throws UnsupportedEncodingException 不支持的编码异常
     */
    private static void packageParam(Map<String, String> params, HttpEntityEnclosingRequestBase httpMethod)
            throws UnsupportedEncodingException {
        // 封装请求参数
        if (params != null) {
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            Set<Entry<String, String>> entrySet = params.entrySet();
            for (Entry<String, String> entry : entrySet) {
                nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }

            // 设置到请求的http对象中
            httpMethod.setEntity(new UrlEncodedFormEntity(nvps, ENCODING));
        }
    }

    /**
     * Description: 获得响应结果
     *
     * @param httpResponse 响应体
     * @return HttpClientResult结果包装类
     */
    private static HttpClientResult getHttpClientResult(CloseableHttpResponse httpResponse) {
        try {
            return new HttpClientResult(httpResponse.getStatusLine().getStatusCode(),
                    EntityUtils.toString(httpResponse.getEntity(), ENCODING));
        } catch (IOException e) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Request submit error.Error info: %s", e));
            return new HttpClientResult(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Description: 释放资源
     *
     * @param httpResponse 响应体
     * @throws IOException 请求异常抛出
     */
    private static void release(CloseableHttpResponse httpResponse) throws IOException {
        // 释放资源
        if (httpResponse != null) {
            httpResponse.close();
        }
    }

    /**
     * 绕过验证
     *
     * @return SSLContext
     * @throws NoSuchAlgorithmException 无对应加密算法
     * @throws KeyManagementException   秘钥异常
     */
    public static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("TLSv1.2");

        // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                String paramString) {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                String paramString) {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        sc.init(null, new TrustManager[]{trustManager}, null);
        return sc;
    }

    private static CloseableHttpClient getClient() {
        if (client == null) {
            createClient();
        }
        return client;
    }

    /**
     * 线程清理，守护线程
     */
    public static class IdleConnectionMonitorThread extends Thread {
        private final HttpClientConnectionManager connMgr;

        private volatile boolean shutdown;

        public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
            super();
            this.connMgr = connMgr;
        }

        /**
         * 定时清理
         */
        @Override
        public void run() {
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(30 * 1000);
                        // Close expired connections
                        connMgr.closeExpiredConnections();
                        // Optionally, close connections
                        // that have been idle longer than 30 sec
                        connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException ex) {
                LOGGER.warning(String.format(Locale.ENGLISH, "Exception in daemon thread. Exception info %s",
                        ex.getMessage()));
                shutdown();
            }
        }

        /**
         * 关闭方法
         */
        public void shutdown() {
            shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }
}
