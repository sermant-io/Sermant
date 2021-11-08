/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.example.demo.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import com.huawei.apm.core.config.BaseConfig;
import com.huawei.apm.core.config.ConfigFieldKey;
import com.huawei.apm.core.config.ConfigTypeKey;

/**
 * 统一配置示例，实现{@link BaseConfig}接口
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/25
 */

@Getter
@Setter
@ConfigTypeKey("demo.test") // 声明前缀
public class DemoConfig implements BaseConfig {

    /**
     * 配置中心实现类
     */
    private String configServerClassName = "com.huawei.apm.core.service.configServer.zookeeper.ZookeeperServer";

    /**
     * 基础类型配置(除byte和char)
     */
    private int intField;

    /**
     * 字符串类型配置
     */
    private String strField;

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
    @ConfigFieldKey("str2DoubleMap") // 修改参数名称后缀
    private Map<String, Double> map;

    /**
     * 枚举类型
     */
    private DemoType enumType;

    @Override
    public String toString() {
        return "DemoConfig{" +
                "intField=" + intField + ", " +
                "strField='" + strField + "', " +
                "shortArr=" + Arrays.toString(shortArr) + ", " +
                "longList=" + longList + ", " +
                "map=" + map + ", " +
                "enumType=" + enumType + "}";
    }
}
