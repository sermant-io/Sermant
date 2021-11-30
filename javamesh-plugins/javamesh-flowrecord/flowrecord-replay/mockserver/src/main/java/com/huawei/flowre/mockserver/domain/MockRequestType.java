/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.mockserver.domain;

import java.util.Objects;

/**
 * 需要做Mock的应用类型，不同的应用类型需要封装成不同的返回值
 * 单例模式 不允许创建新的类型
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-02-03
 */
public class MockRequestType {
    /**
     * http 类型的mock
     */
    public static final MockRequestType HTTP = new MockRequestType("http");

    /**
     * dubbo 类型的mock
     */
    public static final MockRequestType DUBBO = new MockRequestType("dubbo");

    /**
     * mysql 类型的mock
     */
    public static final MockRequestType MYSQL = new MockRequestType("mysql");

    /**
     * redis 类型的mock
     */
    public static final MockRequestType REDIS = new MockRequestType("redis");

    /**
     * custom 类型的mock
     */
    public static final MockRequestType CUSTOM = new MockRequestType("custom");

    /**
     * 不支持该种类型mock或mockserver判断该种接口不进行mock时返回该结果
     */
    public static final MockRequestType NOTYPE = new MockRequestType("NoType");

    private String name;

    private MockRequestType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        MockRequestType that = (MockRequestType) object;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
