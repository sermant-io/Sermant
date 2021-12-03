/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.flowcontrol.console.util;

import com.huawei.flowcontrol.console.datasource.entity.rule.kie.exception.KieGeneralException;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * md5 工具类
 *
 * @author XiaoLong Wang
 * @since 2020-12-21
 */
public class Md5Util {
    private static final int MD5_LENGTH = 32;
    private static final int RADIX = 16;

    private Md5Util() {
    }

    /**
     * 获取md5
     *
     * @param plainText 加密字符串
     * @return 返回md5字符串
     */
    public static String stringToMd5(String plainText) {
        byte[] secretBytes = null;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(
                plainText.getBytes("utf-8"));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new KieGeneralException("MD5 error！");
        }

        StringBuilder md5code = new StringBuilder(new BigInteger(1, secretBytes).toString(RADIX));
        for (int i = 0; i < MD5_LENGTH - md5code.length(); i++) {
            md5code.append("0" + md5code.toString());
        }

        return md5code.toString();
    }
}
