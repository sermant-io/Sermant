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

import com.huawei.register.support.FieldAccessAction;

import java.lang.reflect.Field;
import java.security.AccessController;

/**
 * 公共工具类
 *
 * @author zhouss
 * @since 2021-12-16
 */
public class CommonUtils {
    /**
     * endpoints基于":"分割长度
     */
    private static final int SERVICECOMB_ENDPOINT_PARTS = 3;

    /**
     * ip段所在索引
     */
    private static final int ENDPOINTS_IP_PART_INDEX = 1;

    /**
     * ip前的分隔符长度, 对应实际字符: ' // '
     */
    private static final int ENDPOINTS_SEPARATOR_LEN = 2;

    private CommonUtils() {
    }

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
     * 通过endpoint获取ip 格式: 协议类型://ip:port
     *
     * @param endpoint sc 地址信息
     * @return ip
     */
    public static String getIpByEndpoint(String endpoint) {
        if (endpoint == null) {
            return null;
        }
        final String[] parts = endpoint.split(":");
        if (parts.length == SERVICECOMB_ENDPOINT_PARTS
            && parts[ENDPOINTS_IP_PART_INDEX].length() > ENDPOINTS_SEPARATOR_LEN) {
            return parts[ENDPOINTS_IP_PART_INDEX].substring(ENDPOINTS_SEPARATOR_LEN);
        }
        return null;
    }

    /**
     * 通过反射获取字段值
     *
     * @param target 目标对象
     * @param fieldName 字段名称
     * @return value
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    public static Object getFieldValue(Object target, String fieldName) {
        try {
            final Field fieldValue = target.getClass().getDeclaredField(fieldName);
            AccessController.doPrivileged(new FieldAccessAction(fieldValue));
            return fieldValue.get(target);
        } catch (Exception ex) {
            return null;
        }
    }
}
