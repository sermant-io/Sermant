/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.sermant.registry.utils;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.utils.StringUtils;
import io.sermant.registry.config.ConfigConstants;
import io.sermant.registry.config.RegisterServiceCommonConfig;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Public utility class
 *
 * @author zhouss
 * @since 2021-12-16
 */
public class CommonUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final Consumer<Long> SLEEP = time -> {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ex) {
            LOGGER.fine("Sleep has been interrupted!");
        }
    };

    /**
     * Endpoints based on ":" segmentation length
     */
    private static final int SERVICECOMB_ENDPOINT_PARTS = 3;

    /**
     * The index to which the IP range resides
     */
    private static final int ENDPOINTS_IP_PART_INDEX = 1;

    /**
     * The length of the separator before the IP address, which corresponds to the actual character: ' // '
     */
    private static final int ENDPOINTS_SEPARATOR_LEN = 2;

    private CommonUtils() {
    }

    /**
     * Get the sc endpoint port
     *
     * @param endpoint sc endpoint  rest:
     * @return Port
     */
    public static int getPortByEndpoint(String endpoint) {
        if (endpoint == null) {
            return 0;
        }
        final int index = endpoint.lastIndexOf(':');
        if (index != -1) {
            return Integer.parseInt(endpoint.substring(index + 1));
        }
        return 0;
    }

    /**
     * Obtain an IP format through endpoint: Protocol type://ip:port
     *
     * @param endpoint sc Address information
     * @return ip
     */
    public static Optional<String> getIpByEndpoint(String endpoint) {
        if (endpoint == null) {
            return Optional.empty();
        }
        final String[] parts = endpoint.split(":");
        if (parts.length == SERVICECOMB_ENDPOINT_PARTS
                && parts[ENDPOINTS_IP_PART_INDEX].length() > ENDPOINTS_SEPARATOR_LEN) {
            return Optional.of(parts[ENDPOINTS_IP_PART_INDEX].substring(ENDPOINTS_SEPARATOR_LEN));
        }
        return Optional.empty();
    }

    /**
     * Consume
     *
     * @param consumer Consumer
     * @param target Pass in the target object
     * @param <T> Target type
     */
    public static <T> void accept(Consumer<T> consumer, T target) {
        if (consumer != null) {
            consumer.accept(target);
        }
    }

    /**
     * Sleep for a specified amount of time
     *
     * @param timeMs Sleep time
     */
    public static void sleep(long timeMs) {
        accept(SLEEP, timeMs);
    }

    /**
     * The secure parameter is added to the meta-data
     *
     * @param meta Meta data
     * @param config Public registration configuration
     * @return Meta data
     */
    public static Map<String, String> putSecureToMetaData(Map<String, String> meta,
            RegisterServiceCommonConfig config) {
        if (StringUtils.isEmpty(meta.get(ConfigConstants.SECURE))) {
            meta.put(ConfigConstants.SECURE, String.valueOf(config.isSecure()));
        }
        return meta;
    }
}
