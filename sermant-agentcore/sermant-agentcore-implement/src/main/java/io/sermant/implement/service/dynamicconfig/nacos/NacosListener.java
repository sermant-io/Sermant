/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.implement.service.dynamicconfig.nacos;

import com.alibaba.nacos.api.config.listener.Listener;

import io.sermant.core.service.dynamicconfig.common.DynamicConfigListener;

import java.util.Map;

/**
 * NacosListener Wrapper
 *
 * @author tangle
 * @since 2023-08-17
 */
public class NacosListener {
    /**
     * Listener type: GROUP or KEY
     */
    private String type;

    /**
     * Listener group
     */
    private String group;

    /**
     * Map of listener keys and corresponding Nacos listeners
     */
    private Map<String, Listener> keyListener;

    /**
     * Dynamic configuration listener corresponding to the current listener
     */
    private DynamicConfigListener dynamicConfigListener;

    /**
     * Constructor initializes the listener
     *
     * @param type Listener type
     * @param group Listener group
     * @param keyListener Map of listener keys and corresponding Nacos listeners
     * @param dynamicConfigListener dynamic config listener
     */
    public NacosListener(String type, String group, Map<String, Listener> keyListener,
            DynamicConfigListener dynamicConfigListener) {
        this.type = type;
        this.group = group;
        this.keyListener = keyListener;
        this.dynamicConfigListener = dynamicConfigListener;
    }

    public DynamicConfigListener getDynamicConfigListener() {
        return dynamicConfigListener;
    }

    public void setDynamicConfigListener(
            DynamicConfigListener dynamicConfigListener) {
        this.dynamicConfigListener = dynamicConfigListener;
    }

    public Map<String, Listener> getKeyListener() {
        return keyListener;
    }

    public void setKeyListener(Map<String, Listener> keyListener) {
        this.keyListener = keyListener;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
