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

package com.huawei.sermant.stresstest.config;

/**
 * 配置工厂类
 *
 * @author yiwei
 * @since 2021-10-25
 */
public class ConfigFactory {
    private ConfigFactory() {
    }

    /**
     * 获取配置类，当前只支持文件配置
     *
     * @return 配置类
     */
    public static Config getConfig() {
        return FileConfig.getInstance();
    }
}
