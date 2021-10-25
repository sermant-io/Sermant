/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.oap.server.configuration.zookeeper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64工具类
 *
 * @author zhouss
 * @since 2021-04-05
 **/
public class Base64Utils {
    /**
     * 解码
     *
     * @param val base64字符串
     * @return String
     */
    public static String decode(String val) {
        return new String(Base64.getDecoder().decode(val), StandardCharsets.UTF_8);
    }

    /**
     * base64编码
     *
     * @param val 待编码的字符串
     * @return String
     */
    public static String encode(String val) {
        return Base64.getEncoder().encodeToString(val.getBytes(StandardCharsets.UTF_8));
    }

}
