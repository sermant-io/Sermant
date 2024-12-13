/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.implement.service.xds.entity;

import io.sermant.core.utils.StringUtils;

/**
 * Fraction percentages support several fixed denominator values
 *
 * @author zhp
 * @since 2024-12-10
 **/
public enum DenominatorType {
    /**
     * The HUNDRED means the denominator value is 100
     */
    HUNDRED(100),
    /**
     * The TEN_THOUSAND means the denominator value is 100000
     */
    TEN_THOUSAND(10000),
    /**
     * The MILLION means the denominator value is 1000000
     */
    MILLION(1000000),
    /**
     * UNRECOGNIZED means the denominator value indicates that it cannot fail
     */
    UNRECOGNIZED(0);

    private final int value;

    DenominatorType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    /**
     * Get value based on type name
     *
     * @param name type name
     * @return value
     */
    public static int getValueByName(String name) {
        for (DenominatorType type : DenominatorType.values()) {
            if (StringUtils.equals(name, type.name())) {
                return type.value;
            }
        }
        return UNRECOGNIZED.value;
    }
}
