package com.huawei.apm.core.util;

import static jdk.nashorn.internal.objects.NativeString.valueOf;

public class StringUtils {

    public static final char SLASH_CHAR = '/';

    public static final String SLASH = valueOf(SLASH_CHAR);

    public static final char QUESTION_MASK_CHAR = '?';

    public static final String QUESTION_MASK = valueOf(QUESTION_MASK_CHAR);

    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }

        }
        return true;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
}
