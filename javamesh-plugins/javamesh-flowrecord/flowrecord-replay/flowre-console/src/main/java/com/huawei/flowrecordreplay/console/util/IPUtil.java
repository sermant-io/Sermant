/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.util;

import java.util.regex.Pattern;

/**
 * IP校验
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-02-26
 */
public class IPUtil {
    public static boolean checkIPv4(String str) {
        Pattern pattern = Pattern.compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]"
                + "|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");
        return pattern.matcher(str).matches();
    }
}
