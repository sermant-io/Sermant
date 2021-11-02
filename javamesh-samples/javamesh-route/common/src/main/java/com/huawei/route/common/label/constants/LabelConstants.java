/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.common.label.constants;

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
     * agent-core的标签key
     */
    public static final String AGENT_CORE_LABEL_KEY = "agent-core";

    /**
     * 默认编码
     */
    public static final Charset DEFAULT_CHARSET = Charset.forName("utf-8");

    private LabelConstants() {
    }
}
