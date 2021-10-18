package com.huawei.apm.bootstrap.lubanops.utils;

import java.util.regex.Pattern;

/**
 * @author
 * @date 2020/11/27 11:43
 */
public class ParamCheckUtils {

    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]$");

    public static boolean isUrl(String url) {
        if (StringUtils.isBlank(url) || url.length() > 100) {
            return false;
        }
        if (URL_PATTERN.matcher(url).matches()) {
            return true;
        } else {
            return false;
        }
    }
}
