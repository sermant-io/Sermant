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

package com.huawei.javamesh.core.lubanops.integration.utils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 * 实现二进制和字符串能相互转换的util方法，主要是转换成 下面数组指定的字符，转换后字节长度加倍
 * @author
 * @since 2020/5/11
 **/
public class StringUtil {

    /**
     * 二进制转成可见字符串
     * @param bytes
     * @return
     */
    public static String bytesToString(byte[] bytes) {
        return Hex.encodeHexString(bytes);
    }

    /**
     * 字符串转成二进制
     * @param s
     * @return
     * @throws DecoderException
     */
    public static byte[] stringToBytes(String s) throws DecoderException {
        return Hex.decodeHex(s);
    }

}
