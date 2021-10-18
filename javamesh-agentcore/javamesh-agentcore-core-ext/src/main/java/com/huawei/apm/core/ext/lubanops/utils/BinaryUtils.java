package com.huawei.apm.core.ext.lubanops.utils;

import java.util.Locale;

/**
 * @author
 * @date 2020/8/7 15:09
 */
public class BinaryUtils {

    public static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        byte[] var2 = data;
        int var3 = data.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            byte b = var2[var4];
            String hex = Integer.toHexString(b);
            if (hex.length() == 1) {
                sb.append("0");
            } else if (hex.length() == 8) {
                hex = hex.substring(6);
            }

            sb.append(hex);
        }

        return sb.toString().toLowerCase(Locale.getDefault());
    }

}
