/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.servermonitor.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 计算工具类
 */
public class CalculateUtil {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    public static BigDecimal getPercentage(long numerator, long denominator, int scale) {
        return BigDecimal.valueOf(numerator).multiply(HUNDRED)
            .divide(BigDecimal.valueOf(denominator), scale, RoundingMode.HALF_UP);
    }

}
