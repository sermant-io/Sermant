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

package com.huaweicloud.sermant.router.common.utils;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.router.common.config.TransmitConfig;
import com.huaweicloud.sermant.router.common.request.RequestData;
import com.huaweicloud.sermant.router.common.request.RequestTag;

import java.util.List;
import java.util.Map;

/**
 * Thread variables
 *
 * @author provenceee
 * @since 2022-07-08
 */
public class ThreadLocalUtils {
    private static final ThreadLocal<RequestTag> TAG;

    private static final ThreadLocal<RequestData> DATA;

    private ThreadLocalUtils() {
    }

    static {
        TransmitConfig transmitConfig = PluginConfigManager.getPluginConfig(TransmitConfig.class);
        if (transmitConfig.isEnabledThread()) {
            TAG = new InheritableThreadLocal<>();
            DATA = new InheritableThreadLocal<>();
        } else {
            TAG = new ThreadLocal<>();
            DATA = new ThreadLocal<>();
        }
    }

    /**
     * Get thread variables
     *
     * @return Thread variables
     */
    public static RequestData getRequestData() {
        return DATA.get();
    }

    /**
     * Get thread variables
     *
     * @return Thread variables
     */
    public static RequestTag getRequestTag() {
        return TAG.get();
    }

    /**
     * Deposit thread variables
     *
     * @param value Thread variables
     */
    public static void setRequestData(RequestData value) {
        DATA.set(value);
    }

    /**
     * Deposit thread variables
     *
     * @param value Thread variables
     */
    public static void setRequestTag(RequestTag value) {
        TAG.set(value);
    }

    /**
     * Add request flags in threads
     *
     * @param tag Request tags
     */
    public static void addRequestTag(Map<String, List<String>> tag) {
        if (CollectionUtils.isEmpty(tag)) {
            return;
        }
        RequestTag requestTag = TAG.get();
        if (requestTag == null) {
            TAG.set(new RequestTag(tag));
            return;
        }
        requestTag.addTag(tag);
    }

    /**
     * Delete thread variables
     */
    public static void removeRequestData() {
        DATA.remove();
    }

    /**
     * Delete thread variables
     */
    public static void removeRequestTag() {
        TAG.remove();
    }
}