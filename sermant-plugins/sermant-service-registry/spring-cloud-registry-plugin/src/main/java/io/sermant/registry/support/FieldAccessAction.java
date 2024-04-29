/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.sermant.registry.support;

import java.lang.reflect.Field;
import java.security.PrivilegedAction;

/**
 * Field accessor settings
 *
 * @author zhouss
 * @since 2022-02-24
 */
public class FieldAccessAction implements PrivilegedAction<Object> {
    private final Field field;

    /**
     * Constructor
     *
     * @param field Field
     */
    public FieldAccessAction(Field field) {
        this.field = field;
    }

    @Override
    public Object run() {
        if (field != null) {
            field.setAccessible(true);
        }
        return field;
    }
}
