package com.huawei.user.common.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HttpClientUtils {
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

    static {
        createClient();
    }

    private static synchronized void createClient() {
        if (client != null) {
            return;
        }
        try {
            //采用绕过验证的方式处理https请求
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

            //创建自定义的httpclient对象
            client = HttpClients.custom()
                    .setConnectionManager(connManager)
                    .setConnectionManagerShared(true)
                    .setDefaultRequestConfig(configBuilder.build())
                    .build();
            log.info("PoolingHttpClientConnectionManager init success.");
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("Failed to init httpclient.");
        }
    }

    private HttpClientUtils() {
    }

    /**
     * 绕过验证
     *
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
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

        sc.init(null, new TrustManager[] {trustManager}, null);
        return sc;
    }

    /**
     * 发送get请求；带请求参数
     *
     * @param url 请求地址
     * @param params 请求参数集合
     * @return HttpClientResult结果包装类
     * @throws URISyntaxException
     * @throws IOException
     */
    public static JSONObject doGet(String url, Map<String, String> params)
            throws URISyntaxException, IOException {
        return doGet(url, null, params);
    }

    /**
     * 发送get请求；带请求头和请求参数
     *
     * @param url 请求地址
     * @param headers 请求头集合
     * @param params 请求参数集合
     * @return HttpClientResult结果包装类
     * @throws URISyntaxException
     * @throws IOException
     */
    public static JSONObject doGet(String url, Map<String, String> headers, Map<String, String> params)
            throws URISyntaxException, IOException {
        // 创建httpClient对象
        CloseableHttpClient httpClient = getClient();

        // 创建访问的地址
        URIBuilder uriBuilder = new URIBuilder(url);
        if (params != null) {
            Set<Map.Entry<String, String>> entrySet = params.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                uriBuilder.setParameter(entry.getKey(), entry.getValue());
            }
        }

        // 创建http对象
        HttpGet httpGet = new HttpGet(uriBuilder.build());
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .build();
        httpGet.setConfig(requestConfig);

        // 设置请求头
        packageHeader(headers, httpGet);

        // 创建httpResponse对象
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);

        try {
            // 执行请求并获得响应结果
            return JSONObject.parseObject(EntityUtils.toString(httpResponse.getEntity(), ENCODING));
        } finally {
            // 释放资源
            release(httpResponse);
        }
    }

    /**
     * Description: 封装请求头
     *
     * @param params
     * @param httpMethod
     */
    private static void packageHeader(Map<String, String> params, HttpRequestBase httpMethod) {
        // 封装请求头
        if (params != null) {
            Set<Map.Entry<String, String>> entrySet = params.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                // 设置到请求头到HttpRequestBase对象中
                httpMethod.setHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Description: 释放资源
     *
     * @param httpResponse 响应体
     * @throws IOException io异常
     */
    private static void release(CloseableHttpResponse httpResponse) throws IOException {
        // 释放资源
        if (httpResponse != null) {
            httpResponse.close();
        }
    }

    public static class IdleConnectionMonitorThread extends Thread {

        private final HttpClientConnectionManager connMgr;

        private volatile boolean shutdown;

        public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
            super();
            this.connMgr = connMgr;
        }

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
                log.error("Exception in daemon thread. Exception info {}", ex);
                shutdown();
            }
        }

        public void shutdown() {
            shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }
    public static CloseableHttpClient getClient() {
        if (client == null) {
            createClient();
        }
        return client;
    }
}
