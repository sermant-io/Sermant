/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.register.service.utils;

import java.lang.reflect.Field;

/**
 * 公共工具类
 *
 * @author zhouss
 * @since 2021-12-16
 */
public class CommonUtils {

    /**
     * 获取sc endpoint端口
     *
     * @param endpoint sc endpoint
     * @return 端口
     */
    public static int getPortByEndpoint(String endpoint) {
        if (endpoint == null) {
            return 0;
        }
        final int index = endpoint.lastIndexOf(':');
        if (index != -1) {
            return Integer.parseInt(endpoint.substring(index + 1));
        }
        return 0;
    }

    /**
     * 通过endpoint获取ip
     *
     * @param endpoint sc 地址信息
     * @return ip
     */
    public static String getIpByEndpoint(String endpoint) {
        if (endpoint == null) {
            return null;
        }
        final String[] parts = endpoint.split(":");
        if (parts.length == 3 && parts[1].length() > 2) {
            return parts[1].substring(2);
        }
        return null;
    }

    /**
     * 通过反射获取字段值
     *
     * @param target  目标对象
     * @param fieldName 字段名称
     * @return value
     */
    public static Object getFieldValue(Object target, String fieldName) {
        try {
            final Field fieldValue = target.getClass().getDeclaredField(fieldName);
            fieldValue.setAccessible(true);
            return fieldValue.get(target);
        } catch (Exception ex) {
            return null;
        }
    }
}
