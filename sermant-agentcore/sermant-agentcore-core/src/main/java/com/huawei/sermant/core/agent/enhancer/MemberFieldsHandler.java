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

package com.huawei.sermant.core.agent.enhancer;

import com.huawei.sermant.core.agent.annotations.AboutDelete;
import com.huawei.sermant.core.common.LoggerFactory;

import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.logging.Logger;

/**
 * 处理成员变量
 */
@AboutDelete
@Deprecated
public class MemberFieldsHandler {
    @SuppressWarnings("checkstyle:ModifierOrder")
    private final static Logger LOGGER = LoggerFactory.getLogger();

    private final List<String> fields;

    public MemberFieldsHandler(List<String> fields) {
        this.fields = fields;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @RuntimeType
    public Object intercept(@This Object obj) {
        if (fields == null || fields.isEmpty()) {
            return new Object[0];
        }
        int size = fields.size();
        Object[] convertedFields = new Object[size];
        try {
            for (int i = 0; i < size; i++) {
                final Field declaredField = obj.getClass().getDeclaredField(fields.get(i));
                AccessController.doPrivileged(new FieldAccessibleAction(declaredField));
                convertedFields[i] = declaredField.get(obj);
            }
        } catch (Exception e) {
            LOGGER.warning(
                    String.format("invoke method getLopsFileds failed when convert the member fields! reason:[%s]",
                            e.getMessage()));
        }
        return convertedFields;
    }

    /**
     * 设置字段访问
     */
    static class FieldAccessibleAction implements PrivilegedAction<Object> {
        private final Field field;

        FieldAccessibleAction(Field field) {
            this.field = field;
        }

        @Override
        public Object run() {
            field.setAccessible(true);
            return null;
        }
    }
}
