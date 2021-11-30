package com.huawei.javamesh.core.lubanops.bootstrap.commons;

import java.util.regex.Pattern;

import com.huawei.javamesh.core.lubanops.bootstrap.utils.StringUtils;

/**
 * @author
 * @date 2021/2/4 11:29
 */
public class ValidatorUtil {

    public static void validate(String key, String value, boolean notNull, int length, String regex) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException(String.format("[PARAMETER CHECK] input key[{%s}] is blank.", key));
        }
        if (StringUtils.isBlank(value)) {
            if (notNull) {
                throw new IllegalArgumentException(
                        String.format("[PARAMETER CHECK]value of key[{%s}] can't be empty.", key));
            } else {
                return;
            }
        }
        if (value.length() > length) {
            throw new IllegalArgumentException(
                    String.format("[PARAMETER CHECK] key[{%s}]value of key[{%s}] must contain less than 64 characters.",
                            key, key));
        }
        if (!StringUtils.isBlank(regex)) {
            if (!Pattern.matches(regex, value)) {
                throw new IllegalArgumentException(
                        String.format("[PARAMETER CHECK] key[{%s}] value[%s] contain illegal characters.", key, value));
            }
        }
    }

}
