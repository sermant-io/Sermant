/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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
 * dynamically configure constant classes
 *
 * @author zhouss
 * @since 2022-07-12
 */
public class DynamicConstants {
    /**
     * switch of the original configuration center
     * true : need to close
     * false : need to be opened
     */
    public static final String ORIGIN_CONFIG_CENTER_CLOSE_KEY = "sermant.origin.config.needClose";

    /**
     * Dynamic configuration Primary configuration source name
     */
    public static final String PROPERTY_NAME = "Sermant-Dynamic-Config";

    /**
     * Disable the configuration switch, which is used to determine the startup and associate configurations
     * {@link DynamicConfiguration#isEnableOriginConfigCenter()} ()}
     */
    public static final String DISABLE_CONFIG_SOURCE_NAME = "Sermant-Disable-Origin-Config";

    /**
     * Configure the center to turn off the initial number of listeners
     */
    public static final int CONFIG_CENTER_CLOSER_INIT_NUM = 4;

    private DynamicConstants() {
    }
}
