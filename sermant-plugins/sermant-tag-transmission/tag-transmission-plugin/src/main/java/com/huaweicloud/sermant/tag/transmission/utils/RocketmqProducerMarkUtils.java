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

package com.huaweicloud.sermant.tag.transmission.utils;

/**
 * RocketMQ的生产者线程标记工具类
 *
 * @author lilai
 * @since 2023-09-16
 */
public class RocketmqProducerMarkUtils {
    /**
     * 生产者线程标记
     */
    private static final ThreadLocal<Boolean> PRODUCER_MARK = new ThreadLocal<>();

    private RocketmqProducerMarkUtils() {
    }

    /**
     * 标记当前线程为生产者线程
     */
    public static void setProducerMark() {
        PRODUCER_MARK.set(true);
    }

    /**
     * 判断是否当前线程是否是生产者线程
     *
     * @return 是否是生产者线程
     */
    public static boolean isProducer() {
        return PRODUCER_MARK.get() != null;
    }
}
