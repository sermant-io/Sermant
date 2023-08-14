/*
 *   Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.huaweicloud.sermant.tag.transmission.utils;

import com.huaweicloud.sermant.core.utils.ReflectUtils;

import java.util.Optional;

/**
 * dubbo工具类
 *
 * @author daizhenyu
 * @since 2023-08-03
 **/
public class DubboUtils {
    /**
     * Rpcinvocation类的attachments属性名称
     */
    private static final String ATTACHMENTS_FIELD = "attachments";

    private DubboUtils() {

    }

    /**
     * 使用反射获取Rpcinvocation对象的attachments属性值
     *
     * @param obj Invocation的实现类对象
     * @return Optional dubbo的attachments属性
     */
    public static Optional<Object> getAttachmentsByInvocation(Object obj) {
        return ReflectUtils.getFieldValue(obj, ATTACHMENTS_FIELD);
    }
}