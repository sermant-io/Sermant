/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.stresstest.core;

import com.huawei.sermant.core.utils.StringUtils;
import com.huawei.sermant.stresstest.config.ConfigFactory;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.Locale;

/**
 *  全链路压测标记
 *
 * @author yiwei
 * @since 2021-10-21
 */
public class Tester {
    /**
     * TransmittableThreadLocal 确保除了在本线程内部传递，还能在本地线程之间，本地线程池之间传递。
     */
    private static volatile TransmittableThreadLocal<Boolean> flags = new TransmittableThreadLocal<>();

    private Tester() {
    }

    /**
     * Set test flag.
     *
     * @param test test flag.
     */
    public static void setTest(boolean test) {
        flags.set(test);
    }

    /**
     * Check if the test flag exists.
     *
     * @return true if this is test flag is set, false otherwise.
     */
    public static boolean isTest() {
        Boolean res = flags.get();
        return res != null && res;
    }

    /**
     * 检查当前topic是否是影子topic
     *
     * @param topic 待检查topic
     * @return 影子topic返回true，否则 false
     */
    public static boolean isTestTopic(String topic) {
        return isTestPrefix(topic, ConfigFactory.getConfig().getTestTopicPrefix());
    }

    /**
     * 检查当前字段是否已prefix开头
     *
     * @param topic 待检查字段
     * @param prefix  prefix
     * @return 已prefix开头返回true，否则false
     */
    public static boolean isTestPrefix(String topic, String prefix) {
        return StringUtils.isExist(topic) && topic.toLowerCase(Locale.ROOT).startsWith(prefix);
    }

    /**
     * 给mongodb collection增加影子前缀
     *
     * @param collectionName 原collection
     * @return 修改后的collection
     */
    public static String addTestMongodb(String collectionName) {
        String prefix = ConfigFactory.getConfig().getTestMongodbPrefix();
        return isTestPrefix(collectionName, prefix) ? collectionName : prefix + collectionName;
    }
}
