/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.registry.support;

import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.util.Optional;

/**
 * Test settings field properties
 *
 * @author zhouss
 * @since 2022-09-07
 */
public class FieldAccessActionTest {
    @Test
    public void run() throws NoSuchFieldException {
        final Field testField = TestField.class.getDeclaredField("testField");
        AccessController.doPrivileged(new FieldAccessAction(testField));
        final Optional<Object> override = ReflectUtils.getFieldValue(testField, "override");
        Assert.assertTrue(override.isPresent());
        Assert.assertEquals(override.get(), true);
    }

    static class TestField {
        private Object testField;
    }
}
