/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.example.demo.config;

import com.huawei.javamesh.core.plugin.config.PluginConfig;

/**
 * 插件服务包中定义的插件配置示例，该示例较{@link com.huawei.example.demo.config.DemoConfig}较为简单
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/16
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
