/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.dubbo.registry.service;

import com.huawei.dubbo.registry.utils.ReflectUtils;

import com.huaweicloud.sermant.core.common.LoggerFactory;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/**
 * nacos服务名唤醒
 *
 * @since 2022-10-25
 */
public class NacosServiceNotify {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String ANY_VALUE = "*";

    private static final String DEFAULT_CATEGORY = "providers";

    private static final String CATEGORY_KEY = "category";

    private static final int DEFUALT_CAPACITY = 16;

    private static final String ENABLED_KEY = "enabled";

    private static final String GROUP_KEY = "group";

    private static final String VERSION_KEY = "version";

    private static final String CLASSIFIER_KEY = "classifier";

    private static final String REMOVE_VALUE_PREFIX = "-";

    /**
     * 执行下游无法监听
     *
     * @param url url
     * @param listener 监听
     * @param urls url集合
     */
    public void doNotify(Object url, Object listener, List<Object> urls) {
        if ((CollectionUtils.isEmpty(urls)) && !ANY_VALUE.equals(ReflectUtils.getServiceInterface(url))) {
            LOGGER.warning(String.format(Locale.ENGLISH, "empty notify urls for subscribe url: {%s}", url));
            return;
        }
        Map<String, List<Object>> result = new HashMap<>(DEFUALT_CAPACITY);
        for (Object urlObj : urls) {
            if (isMatch(url, urlObj)) {
                String category = ReflectUtils.getParameter(urlObj, CATEGORY_KEY) == null ? DEFAULT_CATEGORY
                        : ReflectUtils.getParameter(urlObj, CATEGORY_KEY);
                List<Object> categoryList = result.computeIfAbsent(category, key -> new ArrayList<>());
                categoryList.add(urlObj);
            }
        }
        if (result.isEmpty()) {
            return;
        }
        for (Map.Entry<String, List<Object>> entry : result.entrySet()) {
            List<Object> categoryList = new ArrayList<>(entry.getValue());
            ReflectUtils.notify(listener, categoryList);
        }
    }

    private boolean isMatch(Object consumerUrl, Object providerUrl) {
        String consumerInterface = ReflectUtils.getServiceInterface(consumerUrl);
        String providerInterface = ReflectUtils.getServiceInterface(providerUrl);
        if (!(ANY_VALUE.equals(consumerInterface)
                || ANY_VALUE.equals(providerInterface)
                || StringUtils.equals(consumerInterface, providerInterface))) {
            return false;
        }
        String providerCategory = ReflectUtils.getParameter(providerUrl, CATEGORY_KEY) == null ? DEFAULT_CATEGORY
                : ReflectUtils.getParameter(providerUrl, CATEGORY_KEY);
        String consumerCategory = ReflectUtils.getParameter(providerUrl, CATEGORY_KEY) == null ? DEFAULT_CATEGORY
                : ReflectUtils.getParameter(providerUrl, CATEGORY_KEY);
        if (!isMatchCategory(providerCategory, consumerCategory)) {
            return false;
        }
        boolean providerEnabled = ReflectUtils.getParameter(providerUrl, ENABLED_KEY) == null || Boolean
                .parseBoolean(ReflectUtils.getParameter(providerUrl, ENABLED_KEY));
        if (!providerEnabled && !ANY_VALUE.equals(ReflectUtils.getParameter(consumerUrl, ENABLED_KEY))) {
            return false;
        }

        String consumerGroup = ReflectUtils.getParameter(consumerUrl, GROUP_KEY);
        String consumerVersion = ReflectUtils.getParameter(consumerUrl, VERSION_KEY);
        String consumerClassifier = ReflectUtils.getParameter(consumerUrl, CLASSIFIER_KEY) == null ? ANY_VALUE
                : ReflectUtils.getParameter(consumerUrl, CLASSIFIER_KEY);

        String providerGroup = ReflectUtils.getParameter(providerUrl, GROUP_KEY);
        String providerVersion = ReflectUtils.getParameter(providerUrl, VERSION_KEY);
        String providerClassifier = ReflectUtils.getParameter(consumerUrl, CLASSIFIER_KEY) == null ? ANY_VALUE
                : ReflectUtils.getParameter(consumerUrl, CLASSIFIER_KEY);

        boolean checkGroup = ANY_VALUE.equals(consumerGroup)
                || StringUtils.equals(consumerGroup, providerGroup)
                || StringUtils.contains(consumerGroup, providerGroup);
        boolean checkVersion = ANY_VALUE.equals(consumerVersion)
                || StringUtils.equals(consumerVersion, providerVersion);
        boolean checkClassifier = consumerClassifier == null
                || ANY_VALUE.equals(consumerClassifier)
                || StringUtils.equals(consumerClassifier, providerClassifier);
        return checkGroup && checkVersion && checkClassifier;
    }

    private boolean isMatchCategory(String category, String categories) {
        if (StringUtils.isEmpty(categories)) {
            return DEFAULT_CATEGORY.equals(category);
        }
        if (categories.contains(ANY_VALUE)) {
            return true;
        }
        if (categories.contains(REMOVE_VALUE_PREFIX)) {
            return !categories.contains(REMOVE_VALUE_PREFIX + category);
        }
        return categories.contains(category);
    }
}
