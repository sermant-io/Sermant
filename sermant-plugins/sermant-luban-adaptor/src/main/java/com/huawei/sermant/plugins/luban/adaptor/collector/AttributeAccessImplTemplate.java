/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.plugins.luban.adaptor.collector;

import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.plugin.agent.template.ImplDelegator;

import com.lubanops.apm.bootstrap.AttributeAccess;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * {@link AttributeAccess}的委派实现，使用反射的方式构建内部属性集
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-26
 */
public class AttributeAccessImplTemplate extends ImplDelegator.ImplTemplate implements AttributeAccess {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 需要获取的内部属性名称
     */
    private final List<String> fields;

    public AttributeAccessImplTemplate(List<String> fields) {
        this.fields = fields;
    }

    @Override
    public Object[] getLopsFileds() {
        final Object[] result = new Object[fields.size()];
        final Class<?> rawClass = rawObject.getClass();
        for (int i = 0; i < fields.size(); i++) {
            try {
                final Field field = rawClass.getDeclaredField(fields.get(i));
                field.setAccessible(true);
                if (Modifier.isStatic(field.getModifiers())) {
                    result[i] = field.get(null);
                } else {
                    result[i] = field.get(this);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                LOGGER.warning(String.format(Locale.ROOT, "Cannot get field value of %s. ", fields.get(i)));
            }
        }
        return result;
    }
}
