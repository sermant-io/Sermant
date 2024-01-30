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

package com.huawei.registry.config.grace;

import com.huawei.registry.config.GraceConfig;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.utils.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * 优雅上下线工具类
 *
 * @author zhouss
 * @since 2022-05-17
 */
public class GraceHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 获取元信息的方法名称
     */
    private static final String META_METHOD = "getMetadata";

    private GraceHelper() {
    }

    /**
     * 填充预热参数
     *
     * @param meta 服务元信息
     * @param graceConfig 优雅上下线配置
     */
    public static void configWarmUpParams(Map<String, String> meta, GraceConfig graceConfig) {
        if (meta == null || graceConfig == null) {
            LOGGER.warning("Service metadata or Grace config must not be empty!");
            return;
        }
        if (!graceConfig.isEnableWarmUp()) {
            return;
        }
        if (!graceConfig.isWarmUpValid()) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Invalid warm up param, [curve: %s, weight: %s, time: %s]",
                    graceConfig.getWarmUpCurve(), graceConfig.getWarmUpWeight(), graceConfig.getWarmUpTime()));
            return;
        }
        meta.put(GraceConstants.WARM_KEY_CURVE, String.valueOf(graceConfig.getWarmUpCurve()));
        meta.put(GraceConstants.WARM_KEY_INJECT_TIME, String.valueOf(System.currentTimeMillis()));
        meta.put(GraceConstants.WARM_KEY_WEIGHT, String.valueOf(graceConfig.getWarmUpWeight()));
        meta.put(GraceConstants.WARM_KEY_TIME, String.valueOf(graceConfig.getWarmUpTime()));
        LOGGER.info(String.format(Locale.ENGLISH, "Injected warm up params, [curve: %s, weight: %s, time: %s]",
                graceConfig.getWarmUpCurve(), graceConfig.getWarmUpWeight(), graceConfig.getWarmUpTime()));
    }

    /**
     * 基于feign.request#url获取host, 即请求的服务名
     *
     * @param url 请求地址
     * @return 服务名
     */
    public static Optional<String> getServiceNameFromReqUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return Optional.empty();
        }
        try {
            final URI uri = new URI(url);
            return Optional.of(uri.getHost());
        } catch (URISyntaxException ex) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Convert invalid url [%s] to uri", url));
        }
        return Optional.empty();
    }

    /**
     * 获取服务实例元信息
     *
     * @param target 目标对象
     * @return 元信息
     */
    public static Map<String, String> getMetadata(Object target) {
        if (target == null) {
            return Collections.emptyMap();
        }
        try {
            final Method method = target.getClass().getDeclaredMethod(META_METHOD);
            AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                method.setAccessible(true);
                return Optional.empty();
            });
            final Object result = method.invoke(target);
            if (result instanceof Map) {
                return (Map<String, String>) result;
            } else {
                LOGGER.fine("The method named getMetadata is not target method which response type is map!");
            }
        } catch (NoSuchMethodException ex) {
            LOGGER.fine("Get service metadata failed, no method named getMetadata!");
        } catch (InvocationTargetException | IllegalAccessException ex) {
            LOGGER.fine("Get service metadata failed, can not invoke method named getMetadata!");
        }
        return Collections.emptyMap();
    }
}
