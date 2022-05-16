/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.utils;

/**
 * 标记工具类 针对线程标记, 后续优化增加多个变量场景
 *
 * @author zhouss
 * @since 2022-05-07
 */
public class MarkUtils {
    private static final ThreadLocal<Boolean> MARK = new ThreadLocal<>();

    private MarkUtils() {
    }

    /**
     * 标记
     */
    public static void mark() {
        MARK.set(Boolean.TRUE);
    }

    /**
     * 是否标记
     *
     * @return 是否标记
     */
    public static boolean isMarked() {
        return MARK.get() != null;
    }

    /**
     * 清理标记
     */
    public static void unMark() {
        MARK.remove();
    }
}
