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

package com.huaweicloud.sermant.backend.lite.utils;

import java.text.SimpleDateFormat;

/**
 * 时间操作工具
 *
 * @author luanwenfei
 * @since 2022-10-27
 */
public class DateUtil {
    private DateUtil() {
    }

    /**
     * 格式化日期
     *
     * @param times
     * @return 格式化后日期
     */
    public static String getFormatDate(Long times) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(times);
    }
}
