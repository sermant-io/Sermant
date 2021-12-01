package com.huawei.user.common.util;

import org.apache.commons.lang.StringUtils;

/**
 * mysql的模糊查询时特殊字符转义
 */
public class EscapeUtil {
    public static String escapeChar(String before){
        if(StringUtils.isNotBlank(before)){
            before = before.replaceAll("_", "/_");
            before = before.replaceAll("%", "/%");
        }
        return before;
    }
}
