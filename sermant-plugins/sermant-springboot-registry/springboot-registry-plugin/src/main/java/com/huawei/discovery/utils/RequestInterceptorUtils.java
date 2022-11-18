/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.discovery.utils;

import com.huawei.discovery.entity.ServiceInstance;
import com.huawei.discovery.entity.SimpleRequestRecorder;
import com.huawei.discovery.retry.InvokerContext;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 解析url参数、构建公共方法相关工具类
 *
 * @author chengyouling
 * @since 2022-10-09
 */
public class RequestInterceptorUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final SimpleRequestRecorder RECORDER = new SimpleRequestRecorder();

    private static final int URL_INFO_INIT_SIZE = 8;

    /**
     * 解析url时，分隔的最小长度, 其path必须为/serviceName/api/xxx, 否则将不是目标请求 {@link RequestInterceptorUtils#recoverUrl(URL)}
     */
    private static final int MIN_LEN_FOR_VALID_PATH = 2;

    /**
     * 解析url时，分隔的最小长度, 其url必须为http:/www.domain.com/serviceName/api/xxx, 否则将不是目标请求
     * {@link RequestInterceptorUtils#recoverUrl(String)}
     */
    private static final int MIN_LEN_FOR_VALID_URL = 4;

    private RequestInterceptorUtils() {
    }

    private static String formatPath(String path, String query) {
        if (query != null) {
            return path + "?" + query;
        }
        return path;
    }

    /**
     * 解析url参数信息 http://www.domain.com/serviceName/sayHello?name=1
     *
     * @param url url地址
     * @return url解析后的主机名、路径
     */
    public static Map<String, String> recoverUrl(URL url) {
        if (url == null) {
            return Collections.emptyMap();
        }
        final String protocol = url.getProtocol();

        // /serviceName/sayHello?name=1, 已切分为serviceName, sayHello?name=1
        String delim = String.valueOf(HttpConstants.HTTP_URL_SINGLE_SLASH);
        final StringTokenizer tokenizer = new StringTokenizer(url.getPath(), delim);
        if (tokenizer.countTokens() < MIN_LEN_FOR_VALID_PATH) {
            return Collections.emptyMap();
        }
        final Map<String, String> result = new HashMap<>(URL_INFO_INIT_SIZE);
        result.put(HttpConstants.HTTP_URL_SCHEME, protocol);
        result.put(HttpConstants.HTTP_URI_HOST, tokenizer.nextToken(delim));
        result.put(HttpConstants.HTTP_URI_PATH,
                formatPath(tokenizer.nextToken(HttpConstants.EMPTY_STR), url.getQuery()));
        return result;
    }

    /**
     * 解析url参数信息 http://www.domain.com/serviceName/sayHello?name=1
     *
     * @param url url地址
     * @return url解析后的主机名、路径
     */
    public static Map<String, String> recoverUrl(String url) {
        if (StringUtils.isEmpty(url) || !isValidUrl(url)) {
            return Collections.emptyMap();
        }
        String baseSlash = String.valueOf(HttpConstants.HTTP_URL_SINGLE_SLASH);
        final StringTokenizer urlTokens = new StringTokenizer(url, baseSlash);
        if (urlTokens.countTokens() < MIN_LEN_FOR_VALID_URL) {
            return Collections.emptyMap();
        }

        // http(s):.subString(0, len - 1)
        final String rawScheme = urlTokens.nextToken(baseSlash);
        String scheme = rawScheme.substring(0, rawScheme.length() - 1);

        // domain 域名
        urlTokens.nextToken(baseSlash);
        final String serviceName = urlTokens.nextToken(baseSlash);
        final String path = urlTokens.nextToken(HttpConstants.EMPTY_STR);
        Map<String, String> result = new HashMap<>(URL_INFO_INIT_SIZE);
        result.put(HttpConstants.HTTP_URI_HOST, serviceName);
        result.put(HttpConstants.HTTP_URL_SCHEME, scheme);
        result.put(HttpConstants.HTTP_URI_PATH, path);
        return result;
    }

    /**
     * 针对HttpUrlConnection进行地址重构
     *
     * @param originUrl 原地址
     * @param instance 选择的实例
     * @param path 路径
     * @return URL
     */
    public static Optional<URL> rebuildUrlForHttpConnection(URL originUrl, ServiceInstance instance, String path) {
        final String protocol = originUrl.getProtocol();
        String newUrl = buildNewUrl(protocol, instance.getIp(), instance.getPort(), path);
        try {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("[HttpUrlConnection] rebuild url " + newUrl);
            }
            return Optional.of(new URL(newUrl));
        } catch (MalformedURLException e) {
            LOGGER.warning("Can not parse to URL for url " + newUrl);
        }
        return Optional.empty();
    }

    private static String buildNewUrl(String protocol, String ip, int port, String path) {
        return new StringBuilder(protocol)
                .append("://")
                .append(ip)
                .append(":")
                .append(port)
                .append(path)
                .toString();
    }

    /**
     * 打印请求路径
     *
     * @param hostAndPath 请求路径与服务名
     * @param source 请求原， 例如httpclient/http async client
     */
    public static void printRequestLog(String source, Map<String, String> hostAndPath) {
        if (!RECORDER.isEnable()) {
            return;
        }
        String path = String.format(Locale.ENGLISH, "/%s%s", hostAndPath.get(HttpConstants.HTTP_URI_HOST),
            hostAndPath.get(HttpConstants.HTTP_URI_PATH));
        LOGGER.log(Level.FINE, String.format(Locale.ENGLISH, "[%s] request [%s] has been intercepted!", source, path));
        RECORDER.beforeRequest();
    }

    /**
     * 格式化uri
     *
     * @param uri 目标uri
     * @return URI
     */
    public static Optional<URI> formatUri(String uri) {
        if (!isValidUrl(uri)) {
            return Optional.empty();
        }
        try {
            return Optional.of(new URI(uri));
        } catch (URISyntaxException e) {
            LOGGER.fine(String.format(Locale.ENGLISH, "%s is not valid uri!", uri));
            return Optional.empty();
        }
    }

    private static boolean isValidUrl(String url) {
        final String lowerCaseUrl = url.toLowerCase(Locale.ROOT);
        return lowerCaseUrl.startsWith("http") || lowerCaseUrl.startsWith("https");
    }

    /**
     * 构建invoke回调方法函数
     *
     * @param context 上下文
     * @param invokerContext 调用上下文
     * @return 调用器
     */
    public static Supplier<Object> buildFunc(ExecuteContext context, InvokerContext invokerContext) {
        return buildFunc(context.getObject(), context.getMethod(), context.getArguments(), invokerContext);
    }

    /**
     * 构建invoke回调方法函数
     *
     * @param target 目标
     * @param arguments 参数
     * @param method 方法
     * @param invokerContext 调用上下文
     * @return 调用器
     */
    public static Supplier<Object> buildFunc(Object target, Method method, Object[] arguments,
            InvokerContext invokerContext) {
        return () -> {
            try {
                AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                    method.setAccessible(true);
                    return method;
                });
                return method.invoke(target, arguments);
            } catch (IllegalAccessException e) {
                LOGGER.log(Level.SEVERE, String.format(Locale.ENGLISH, "Can not invoke method [%s]",
                        method.getName()), e);
            } catch (InvocationTargetException e) {
                invokerContext.setEx(e.getTargetException());
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, String.format(Locale.ENGLISH, "invoke method [%s] failed",
                            method.getName()), e.getTargetException());
                }
            }
            return Optional.empty();
        };
    }

    /**
     * 构建ip+端口url
     *
     * @param urlIfo url信息, 包含host, path
     * @param serviceInstance 选择的实例
     * @return url
     */
    public static String buildUrl(Map<String, String> urlIfo, ServiceInstance serviceInstance) {
        StringBuilder urlBuild = new StringBuilder();
        urlBuild.append(urlIfo.get(HttpConstants.HTTP_URL_SCHEME))
            .append(HttpConstants.HTTP_URL_DOUBLE_SLASH)
            .append(serviceInstance.getIp())
            .append(HttpConstants.HTTP_URL_COLON)
            .append(serviceInstance.getPort())
            .append(urlIfo.get(HttpConstants.HTTP_URI_PATH));
        return urlBuild.toString();
    }

    /**
     * 解析host、path信息
     *
     * @param path 请求路径
     * @return host、path信息集合
     */
    public static Map<String, String> recoverHostAndPath(String path) {
        Map<String, String> result = new HashMap<>(URL_INFO_INIT_SIZE);
        if (StringUtils.isEmpty(path)) {
            return result;
        }
        int startIndex = 0;
        while (startIndex < path.length() && path.charAt(startIndex) == HttpConstants.HTTP_URL_SINGLE_SLASH) {
            startIndex++;
        }
        String tempPath = path.substring(startIndex);
        if (tempPath.indexOf(HttpConstants.HTTP_URL_SINGLE_SLASH) <= 0) {
            return result;
        }
        result.put(HttpConstants.HTTP_URI_HOST,
            tempPath.substring(0, tempPath.indexOf(HttpConstants.HTTP_URL_SINGLE_SLASH)));
        result.put(HttpConstants.HTTP_URI_PATH,
            tempPath.substring(tempPath.indexOf(HttpConstants.HTTP_URL_SINGLE_SLASH)));
        return result;
    }

    /**
     * 构建包含ip、端口url
     *
     * @param uri uri信息
     * @param serviceInstance 选择的实例
     * @param path 路径
     * @param method 反复类型
     * @return ip:port构建的url
     */
    public static String buildUrlWithIp(URI uri, ServiceInstance serviceInstance, String path, String method) {
        StringBuilder urlBuild = new StringBuilder();
        urlBuild.append(uri.getScheme())
            .append(HttpConstants.HTTP_URL_DOUBLE_SLASH)
            .append(serviceInstance.getIp())
            .append(HttpConstants.HTTP_URL_COLON)
            .append(serviceInstance.getPort())
            .append(path);
        if (uri.getQuery() != null && method.equals(HttpConstants.HTTP_GET)) {
            urlBuild.append(HttpConstants.HTTP_URL_UNKNOWN)
                .append(uri.getQuery());
        }
        return urlBuild.toString();
    }

    /**
     * 构建包含ip、端口url
     *
     * @param hostAndPath 请求信息
     * @param ip ip地址
     * @param port 端口
     * @return ip:port构建的url
     */
    public static String buildUrlWithIp(Map<String, String> hostAndPath, String ip, int port) {
        return hostAndPath.get(HttpConstants.HTTP_URL_SCHEME)
                + HttpConstants.HTTP_URL_DOUBLE_SLASH
                + ip
                + HttpConstants.HTTP_URL_COLON
                + port
                + hostAndPath.get(HttpConstants.HTTP_URI_PATH);
    }
}
