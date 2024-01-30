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
 * 线程变量标记类，用于兼容kafka不同版本不重复进入构造函数
 *
 * @author lilai
 * @since 2023-12-09
 */
public class MarkUtils {
    private static final ThreadLocal<Boolean> MARK = new ThreadLocal<>();

    private MarkUtils() {
    }

    /**
     * 获取线程变量
     *
     * @return 线程变量
     */
    public static Boolean getMark() {
        return MARK.get();
    }

    /**
     * 存入线程变量
     *
     * @param value 线程变量
     */
    public static void setMark(Boolean value) {
        MARK.set(value);
    }

    /**
     * 移除线程变量
     *
     */
    public static void removeMark() {
        MARK.remove();
    }
}