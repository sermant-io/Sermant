/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.router.common.metric;

/**
 * Thread-local variables, Store the flag for metric collection to prevent duplicate collection
 *
 * @author zhp
 * @since 2024-10-16
 */
public class MetricThreadLocal {
    private static final ThreadLocal<Boolean> FLAG = new ThreadLocal<>();

    /**
     * Constructor
     */
    private MetricThreadLocal() {
    }

    /**
     * Set flag information
     *
     * @param flag execute flag
     */
    public static void setFlag(Boolean flag) {
        FLAG.set(flag);
    }

    /**
     * Get flag information
     *
     * @return flag information
     */
    public static boolean getFlag() {
        return FLAG.get() != null && FLAG.get();
    }

    /**
     * remove flag information
     */
    public static void removeFlag() {
        FLAG.remove();
    }
}
