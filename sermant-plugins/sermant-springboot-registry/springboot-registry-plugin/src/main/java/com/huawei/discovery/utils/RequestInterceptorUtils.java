/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parse URL parameters and build utility classes related to public methods
 *
 * @author chengyouling
 * @since 2022-10-09
 */
public class RequestInterceptorUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final SimpleRequestRecorder RECORDER = new SimpleRequestRecorder();

    private static final int URL_INFO_INIT_SIZE = 8;

    /**
     * When parsing a URL, the minimum length of the delimitation, whose path must be /serviceName/api/xxx, otherwise it
     * will not be the target request {@link RequestInterceptorUtils#recoverUrl(URL)}
     */
    private static final int MIN_LEN_FOR_VALID_PATH = 2;

    /**
     * When parsing URLs, the minimum length of the delimitation, its URL must be
     * http:/www.domain.com/serviceName/api/xxx, otherwise it will not be the target request
     * {@link RequestInterceptorUtils#recoverUrl(String)}
     */
    private static final int MIN_LEN_FOR_VALID_URL = 4;

    private static final Map<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();

    private RequestInterceptorUtils() {
    }

    private static String formatPath(String path, String query) {
        if (query != null) {
            return path + "?" + query;
        }
        return path;
    }

    /**
     * Parsing URL parameter information http://www.domain.com/serviceName/sayHello?name=1
     *
     * @param url URL address
     * @return The hostname and path of the URL resolution
     */
    public static Map<String, String> recoverUrl(URL url) {
        if (url == null) {
            return Collections.emptyMap();
        }
        final String protocol = url.getProtocol();

        // /serviceName/sayHello?name=1, Split into serviceName, sayHello?name=1
        String delim = String.valueOf(HttpConstants.HTTP_URL_SINGLE_SLASH);
        final StringTokenizer tokenizer = new StringTokenizer(url.getPath(), delim);
        if (tokenizer.countTokens() < MIN_LEN_FOR_VALID_PATH) {
            return Collections.emptyMap();
        }
        final Map<String, String> result = new HashMap<>(URL_INFO_INIT_SIZE);
        result.put(HttpConstants.HTTP_URL_SCHEME, protocol);
        result.put(HttpConstants.HTTP_URI_SERVICE, tokenizer.nextToken(delim));
        result.put(HttpConstants.HTTP_URI_PATH,
                formatPath(tokenizer.nextToken(HttpConstants.EMPTY_STR), url.getQuery()));
        return result;
    }

    /**
     * Parsing URL parameter information http://www.domain.com/serviceName/sayHello?name=1
     *
     * @param url URL address
     * @return The hostname and path of the URL resolution
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

        // domain name
        Map<String, String> result = new HashMap<>(URL_INFO_INIT_SIZE);
        result.put(HttpConstants.HTTP_URI_HOST, urlTokens.nextToken(baseSlash));
        final String serviceName = urlTokens.nextToken(baseSlash);
        final String path = urlTokens.nextToken(HttpConstants.EMPTY_STR);
        result.put(HttpConstants.HTTP_URI_SERVICE, serviceName);
        result.put(HttpConstants.HTTP_URL_SCHEME, scheme);
        result.put(HttpConstants.HTTP_URI_PATH, path);
        return result;
    }

    /**
     * Address reconstruction for HttpUrlConnection
     *
     * @param originUrl Original address
     * @param instance Selected instances
     * @param path Path
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
     * Print the request path
     *
     * @param hostAndPath The request path and service name
     * @param source Request original, For example httpclient/http async client
     */
    public static void printRequestLog(String source, Map<String, String> hostAndPath) {
        if (!RECORDER.isEnable()) {
            return;
        }
        String path = String.format(Locale.ENGLISH, "/%s%s", hostAndPath.get(HttpConstants.HTTP_URI_SERVICE),
                hostAndPath.get(HttpConstants.HTTP_URI_PATH));
        LOGGER.log(Level.FINE, String.format(Locale.ENGLISH, "[%s] request [%s] has been intercepted!", source, path));
        RECORDER.beforeRequest();
    }

    /**
     * Format the URI
     *
     * @param uri Destination URI
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
     * Build an invoke callback method function
     *
     * @param context Context
     * @param invokerContext call context
     * @return Invoker
     */
    public static Supplier<Object> buildFunc(ExecuteContext context, InvokerContext invokerContext) {
        return buildFunc(context.getObject(), context.getMethod(), context.getArguments(), invokerContext);
    }

    /**
     * Build an invoke callback method function
     *
     * @param target Target
     * @param arguments Parameter
     * @param method method
     * @param invokerContext call context
     * @return Invoker
     */
    public static Supplier<Object> buildFunc(Object target, Method method, Object[] arguments,
            InvokerContext invokerContext) {
        return () -> {
            try {
                return METHOD_CACHE.computeIfAbsent(method.toString(),
                        key -> AccessController.doPrivileged((PrivilegedAction<Method>) () -> {
                            method.setAccessible(true);
                            return method;
                        })).invoke(target, arguments);
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
     * Build an IP+ port URL
     *
     * @param urlIfo URL information, including host, path
     * @param serviceInstance Selected instances
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
     * Parse host and path information
     *
     * @param path The path of the request
     * @return host、path information collection
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
        int indexOf = tempPath.indexOf(HttpConstants.HTTP_URL_SINGLE_SLASH);
        if (indexOf <= 0) {
            return result;
        }
        result.put(HttpConstants.HTTP_URI_SERVICE, tempPath.substring(0, indexOf));
        result.put(HttpConstants.HTTP_URI_PATH, tempPath.substring(indexOf));
        return result;
    }

    /**
     * The build contains the IP address and port URL
     *
     * @param uri URI information
     * @param serviceInstance Selected instances
     * @param path Path
     * @param method Repeated type
     * @return The URL constructed by ip: port
     */
    public static String buildUrlWithIp(URI uri, ServiceInstance serviceInstance, String path, String method) {
        StringBuilder urlBuild = new StringBuilder();
        urlBuild.append(uri.getScheme())
                .append(HttpConstants.HTTP_URL_DOUBLE_SLASH)
                .append(serviceInstance.getIp())
                .append(HttpConstants.HTTP_URL_COLON)
                .append(serviceInstance.getPort())
                .append(path);
        if (uri.getRawQuery() != null) {
            urlBuild.append(HttpConstants.HTTP_URL_UNKNOWN).append(uri.getRawQuery());
        }
        return urlBuild.toString();
    }

    /**
     * The build contains the IP address and port URL
     *
     * @param hostAndPath Request Information
     * @param ip IP address
     * @param port Port
     * @return The URL constructed by ip: port
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
