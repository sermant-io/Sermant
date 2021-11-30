package com.huawei.flowrecord.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class base64Util {

    private static final Base64.Decoder DECODER = java.util.Base64.getDecoder();
    private static final Base64.Encoder ENCODER = java.util.Base64.getEncoder();

    public static String decode2UTFString(String in) {
        return new String(DECODER.decode(in), StandardCharsets.UTF_8);
    }

    public static String encode(String text) {
        return ENCODER.encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

}
