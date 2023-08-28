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

package com.huaweicloud.sermant.implement.service.dynamicconfig.nacos;

import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigListener;

import com.alibaba.nacos.api.config.listener.Listener;

import java.util.Map;

/**
 * Nacos监听器包装类
 *
 * @author tangle
 * @since 2023-08-17
 */
public class NacosListener {
    /**
     * 监听器类型：GROUP和KEY两种类型
     */
    private String type;

    /**
     * 监听器组
     */
    private String group;

    /**
     * 监听器key和对应的nacos监听器组成的Map
     */
    private Map<String, Listener> keyListener;

    /**
     * 当前监听器所对应的动态配置监听器
     */
    private DynamicConfigListener dynamicConfigListener;

    /**
     * 构造函数初始化监听器
     *
     * @param type 监听器类型
     * @param group 监听器组名
     * @param keyListener 监听器的key和对应的nacos提供的监听器组成的map
     * @param dynamicConfigListener 动态配置监听器
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
