/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.example.demo.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.huawei.javamesh.core.config.common.ConfigFieldKey;
import com.huawei.javamesh.core.config.common.ConfigTypeKey;
import com.huawei.javamesh.core.plugin.config.AliaConfig;

/**
 * 统一配置示例
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/25
 */
@ConfigTypeKey("demo.test") // 声明前缀
public class DemoConfig extends AliaConfig { // 有设置拦截器别名需求的继承AliaConfig，否则实现PluginConfig
    /**
     * 基础类型配置(除byte和char)
     */
    private int intField;

    /**
     * 字符串类型配置
     */
    private String strField;

    /**
     * 复杂类型配置
     */
    private DemoPojo pojoField;

    /**
     * 数组类型，子类型为基础类型、字符串或枚举
     */
    private short[] shortArr;

    /**
     * 集合类型，子类型为基础类型、字符串或枚举
     */
    private List<Long> longList;

    /**
     * 字典类型，子类型为基础类型、字符串或枚举
     */
    @ConfigFieldKey("str2DemoSimplePojoMap") // 为属性起别名
    private Map<String, DemoPojo> map;

    /**
     * 枚举类型
     */
    private DemoType enumType;

    public int getIntField() {
        return intField;
    }

    public void setIntField(int intField) {
        this.intField = intField;
    }

    public String getStrField() {
        return strField;
    }

    public void setStrField(String strField) {
        this.strField = strField;
    }

    public DemoPojo getPojoField() {
        return pojoField;
    }

    public void setPojoField(DemoPojo pojoField) {
        this.pojoField = pojoField;
    }

    public short[] getShortArr() {
        return shortArr;
    }

    public void setShortArr(short[] shortArr) {
        this.shortArr = shortArr;
    }

    public List<Long> getLongList() {
        return longList;
    }

    public void setLongList(List<Long> longList) {
        this.longList = longList;
    }

    public Map<String, DemoPojo> getMap() {
        return map;
    }

    public void setMap(Map<String, DemoPojo> map) {
        this.map = map;
    }

    public DemoType getEnumType() {
        return enumType;
    }

    public void setEnumType(DemoType enumType) {
        this.enumType = enumType;
    }

    @Override
    public String toString() {
        return "DemoConfig{" +
                "intField=" + intField +
                ", strField='" + strField + '\'' +
                ", demoComplexPojoField=" + pojoField +
                ", shortArr=" + Arrays.toString(shortArr) +
                ", longList=" + longList +
                ", map=" + map +
                ", enumType=" + enumType +
                "} " + super.toString();
    }
}
