/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.example.demo.config;

import com.huawei.sermant.core.plugin.config.PluginConfig;

/**
 * 插件服务包中定义的插件配置示例，该示例较{@link com.huawei.example.demo.config.DemoConfig}较为简单
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-16
 */
public class DemoServiceConfig implements PluginConfig { // 没有设定类别名的情况，将使用类的全限定名
    private String testField;

    public String getTestField() {
        return testField;
    }

    public void setTestField(String testField) {
        this.testField = testField;
    }

    @Override
    public String toString() {
        return "DemoServiceConfig{" +
                "testField='" + testField + '\'' +
                '}';
    }
}
