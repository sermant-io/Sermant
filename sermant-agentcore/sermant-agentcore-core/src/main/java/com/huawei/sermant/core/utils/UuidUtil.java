/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.core.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * uuid
 *
 * @author xuezechao
 * @since 2022-02-28
 */
public class UuidUtil {

    private UuidUtil() {
    }

    /**
     * 生成Long 类型唯一ID
     *
     * @return uuid
     */
    public static long getId() {
        long nowTime = Long.parseLong(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
        AtomicLong instanceId = new AtomicLong(nowTime);
        if (instanceId.get() < 0) {
            return -instanceId.get();
        }
        return instanceId.get();
    }
}
