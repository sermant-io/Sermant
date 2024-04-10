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
 * Elegant online and offline tools
 *
 * @author zhouss
 * @since 2022-05-17
 */
public class GraceHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * The name of the method to get the meta information
     */
    private static final String META_METHOD = "getMetadata";

    private GraceHelper() {
    }

    /**
     * Populate the preheating parameters
     *
     * @param meta Service meta information
     * @param graceConfig Elegant online and offline configuration
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
     * Obtain the host, that is, the name of the requested service, based on feign.request#url
     *
     * @param url The address of the request
     * @return Service name
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
     * Obtain the meta-information of the service instance
     *
     * @param target Target Audience
     * @return Meta information
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
