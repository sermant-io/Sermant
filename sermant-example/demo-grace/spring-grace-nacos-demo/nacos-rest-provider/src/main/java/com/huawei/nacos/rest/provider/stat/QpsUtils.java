/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.nacos.rest.provider.stat;

/**
 * QPS工具类
 *
 * @author zhouss
 * @since 2022-06-20
 */
public class QpsUtils {
    private static final ThreadLocal<Integer> LOCAL = new ThreadLocal<>();

    private QpsUtils() {
    }

    /**
     * 保存QPS
     *
     * @param passQps qps
     */
    public static void set(int passQps) {
        LOCAL.set(passQps);
    }

    /**
     * 获取qps
     *
     * @return qps
     */
    public static Integer get() {
        return LOCAL.get();
    }

    /**
     * 移除QPS
     */
    public static void remove() {
        LOCAL.remove();
    }
}
