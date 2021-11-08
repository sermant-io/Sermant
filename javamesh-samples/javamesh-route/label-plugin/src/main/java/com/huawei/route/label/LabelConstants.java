/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.label;

import java.nio.charset.Charset;

/**
 * 常量类
 *
 * @author Zhang Hu
 * @since 2020-10-13
 */
public class LabelConstants {
    /**
     * true的字符串
     */
    public static final String STRING_FOR_TRUE = "true";

    /**
     * false的字符串
     */
    public static final String STRING_FOR_FALSE = "false";

    /**
     * 标签名的key
     */
    public static final String LABEL_NAME_KEY = "labelName";

    /**
     * 鲁班默认
     */
    public static final String DEFAULT_INSTANCE_NAME = "default";

    /**
     * 生成实例名的uuid长度
     */
    public static final int INSTANCE_NAME_PREFIX_LEN = 8;

    /**
     * 默认编码
     */
    public static final Charset DEFAULT_CHARSET = Charset.forName("utf-8");

    private LabelConstants() {
    }
}
