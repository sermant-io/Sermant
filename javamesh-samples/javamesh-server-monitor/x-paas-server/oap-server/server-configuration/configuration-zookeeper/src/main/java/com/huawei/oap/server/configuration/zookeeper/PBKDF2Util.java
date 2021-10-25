/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.oap.server.configuration.zookeeper;

import org.apache.skywalking.apm.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Locale;

/**
 * PBKDF2加密与检验工具类
 *
 * @author zhouss
 * @since 2021-04-13
 **/
public class PBKDF2Util {
    private static final Logger LOGGER = LoggerFactory.getLogger(PBKDF2Util.class);

    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";

    /**
     * 盐的长度
     */
    private static final int SALT_BYTE_SIZE = 32 / 2;

    /**
     * 生成密文的长度
     */
    private static final int HASH_BIT_SIZE = 128 * 4;

    /**
     * 迭代次数
     */
    private static final int PBKDF2_ITERATIONS = 10000;

    /**
     * 16进制
     */
    private static final int HEX_RADIX = 16;

    /**
     * 对输入的password进行验证
     *
     * @param attemptedPassword 待验证的password
     * @param encryptedPassword 密文
     * @param salt              盐值
     * @return 是否验证成功
     */
    public static boolean authenticate(String attemptedPassword, String encryptedPassword, String salt) {
        // 用同样的盐值对用户输入的password进行加密
        String encryptedAttemptedPassword = getEncryptedPassword(attemptedPassword, salt);
        // 把加密后的密文和原密文进行比較，同样则验证成功。否则失败
        return encryptedAttemptedPassword.equals(encryptedPassword);
    }

    /**
     * 生成密文
     *
     * @param password 明文password
     * @param salt     盐值
     * @return encrypted password
     */
    public static String getEncryptedPassword(String password, String salt) {
        if (StringUtil.isEmpty(password) || StringUtil.isEmpty(salt)) {
            throw new IllegalArgumentException("error params for encrypt");
        }
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), fromHex(salt), PBKDF2_ITERATIONS, HASH_BIT_SIZE);
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            return toHex(secretKeyFactory.generateSecret(spec).getEncoded());
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            LOGGER.error("encrypt password with pbkdf2 failed!", e);
        }
        return "";
    }

    /**
     * 通过提供加密的强随机数生成器 生成盐
     *
     * @return salt
     */
    public static String generateSalt() {
        SecureRandom random = null;
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("no such algorithm!", e);
            return "";
        }
        byte[] salt = new byte[SALT_BYTE_SIZE];
        random.nextBytes(salt);

        return toHex(salt);
    }

    /**
     * 十六进制字符串转二进制字符串
     *
     * @param hex 待转换为数组的16进制字符串
     * @return byte[]
     */
    private static byte[] fromHex(String hex) {
        byte[] binary = new byte[hex.length() / 2];
        for (int i = 0; i < binary.length; i++) {
            binary[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), HEX_RADIX);
        }
        return binary;
    }

    /**
     * 二进制字符串转十六进制字符串
     *
     * @param array 待转换为为16进制的字节数组
     * @return String
     */
    private static String toHex(byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(HEX_RADIX);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {
            return String.format(Locale.ENGLISH, "%0" + paddingLength + "d", 0) + hex;
        }
        return hex;
    }
}
