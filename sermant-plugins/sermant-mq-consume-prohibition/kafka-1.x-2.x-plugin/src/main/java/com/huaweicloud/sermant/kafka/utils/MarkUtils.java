/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.kafka.utils;

/**
 * Thread variable marker class, used to be compatible with different versions of Kafka and not to repeatedly enter
 * constructors
 *
 * @author lilai
 * @since 2023-12-09
 */
public class MarkUtils {
    private static final ThreadLocal<Boolean> MARK = new ThreadLocal<>();

    private MarkUtils() {
    }

    /**
     * Get thread variables
     *
     * @return Thread variables
     */
    public static Boolean getMark() {
        return MARK.get();
    }

    /**
     * Store thread variables
     *
     * @param value Thread variables
     */
    public static void setMark(Boolean value) {
        MARK.set(value);
    }

    /**
     * Remove thread variables
     */
    public static void removeMark() {
        MARK.remove();
    }
}