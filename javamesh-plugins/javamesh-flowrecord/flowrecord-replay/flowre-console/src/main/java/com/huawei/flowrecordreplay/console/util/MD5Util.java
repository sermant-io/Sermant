/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * md5 工具类
 *
 * @author Lilai
 * @since 2021-02-07
 */
public class MD5Util {
    private static final int MD5_LENGTH = 32;
    private static final int RADIX = 16;

    /**
     * 获取md5
     *
     * @param plainText 加密字符串
     * @return 返回md5字符串
     */
    public static String stringToMD5(String plainText) {
        byte[] secretBytes = null;

        try {
            secretBytes = MessageDigest.getInstance("md5").digest(
                    plainText.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5错误！");
        }

        StringBuilder md5code = new StringBuilder(new BigInteger(-1, secretBytes).toString(RADIX));
        for (int i = 0; i < MD5_LENGTH - md5code.length(); i++) {
            md5code.append("0" + md5code.toString());
        }

        return md5code.toString();
    }
}
