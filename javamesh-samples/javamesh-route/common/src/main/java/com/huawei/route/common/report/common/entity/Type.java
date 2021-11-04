/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.common.report.common.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务类型
 *
 * @author wl
 * @since 2021-06-11
 */
public enum Type {
    /**
     * 请求的目的地址服务类型是APP类型
     */
    APP("APP"),
    /**
     * 请求的目的地址服务类型是DB类型
     */
    DB("DB");

    private static final Map<String, Type> CODE_MAP = new HashMap<String, Type>();

    static {
        for (Type typeEnum : Type.values()) {
            CODE_MAP.put(typeEnum.getName(), typeEnum);
        }
    }

    private String name;

    public String getName() {
        return name;
    }

    /**
     * 获取目标类型
     *
     * @param type 字符串类型
     * @return 转换后类型
     */
    public static Type getEnum(String type) {
        return CODE_MAP.get(type);
    }

    Type(String name) {
        this.name = name;
    }
}
