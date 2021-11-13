package com.huawei.common.util;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class PasswordUtil {
    @Value("${key}")
    private String key;

    public String encodePassword(String password) throws UnsupportedEncodingException {
        AES aes = SecureUtil.aes(key.getBytes(StandardCharsets.UTF_8));

        // AES加密
        byte[] encrypt = aes.encrypt(password);

        // Base64加密
        byte[] encode = Base64.getEncoder().encode(encrypt);
        return new String(encode, "utf-8");
    }

    public String decodePassword(String password) throws UnsupportedEncodingException {
        AES aes = SecureUtil.aes(key.getBytes(StandardCharsets.UTF_8));
        byte[] decode = Base64.getDecoder().decode(password);
        byte[] decrypt = aes.decrypt(decode);
        return new String(decrypt, "utf-8");
    }
}
