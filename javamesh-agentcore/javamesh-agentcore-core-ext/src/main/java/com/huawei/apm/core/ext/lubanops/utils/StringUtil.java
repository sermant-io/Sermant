package com.huawei.apm.core.ext.lubanops.utils;

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
