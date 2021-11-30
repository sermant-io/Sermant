/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.example.demo.config;

import com.huawei.javamesh.core.config.common.ConfigFieldKey;

/**
 * 插件配置中的复杂对象示例
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/16
 */
public class DemoPojo {
    /**
     * 普通属性
     */
    private int intField;

    /**
     * 起别名的属性，将作为检验别名是否生效的依据
     */
    @ConfigFieldKey("strField") // 修改参数名称后缀
    private String str;

    public int getIntField() {
        return intField;
    }

    public void setIntField(int intField) {
        this.intField = intField;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return "DemoComplexPojo{" +
                "intField=" + intField +
                ", str='" + str + '\'' +
                '}';
    }
}
