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

package com.huawei.dynamic.config.entity;

import com.huawei.dynamic.config.DynamicConfiguration;

/**
 * 动态配置常量类
 *
 * @author zhouss
 * @since 2022-07-12
 */
public class DynamicConstants {
    /**
     * 原配置中心配置开关
     * true : 需要关闭
     * false : 需开启
     */
    public static final String ORIGIN_CONFIG_CENTER_CLOSE_KEY = "sermant.origin.config.needClose";

    /**
     * 动态配置主配置源名称
     */
    public static final String PROPERTY_NAME = "Sermant-Dynamic-Config";

    /**
     * 禁用配置开关, 用于启动时判断, 关联配置{@link DynamicConfiguration#isEnableOriginConfigCenter()} ()}
     */
    public static final String DISABLE_CONFIG_SOURCE_NAME = "Sermant-Disable-Origin-Config";

    /**
     * 配置中心关闭监听器初始数
     */
    public static final int CONFIG_CENTER_CLOSER_INIT_NUM = 4;

    private DynamicConstants() {
    }
}
