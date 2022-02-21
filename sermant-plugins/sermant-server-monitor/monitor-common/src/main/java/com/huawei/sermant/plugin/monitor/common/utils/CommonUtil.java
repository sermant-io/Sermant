/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.sermant.plugin.monitor.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Common工具类
 */
public class CommonUtil {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    public static BigDecimal getPercentage(long numerator, long denominator, int scale) {
        return BigDecimal.valueOf(numerator).multiply(HUNDRED)
                .divide(BigDecimal.valueOf(denominator), scale, RoundingMode.HALF_UP);
    }

    public static String getStackTrace(Throwable t) {
        ByteArrayOutputStream byteArrayOutputStream = null;
        PrintStream printStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            printStream = new PrintStream(byteArrayOutputStream);
            t.printStackTrace(printStream);
            return byteArrayOutputStream.toString();
        } finally {
            if (printStream != null) {
                printStream.close();
            }
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    // ignored
                }
            }
        }
    }
}
