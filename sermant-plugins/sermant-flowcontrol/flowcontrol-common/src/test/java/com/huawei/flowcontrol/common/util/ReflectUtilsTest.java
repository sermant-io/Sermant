/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.common.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

/**
 * 反射工具类测试
 *
 * @author zhouss
 * @since 2022-08-29
 */
public class ReflectUtilsTest {
    /**
     * 测试定义类
     */
    @Test
    public void testDefine() {
        final Optional<Class<?>> aClass = ReflectUtils.defineClass("com.est.error");
        Assert.assertFalse(aClass.isPresent());
        final Optional<Class<?>> stringClazz = ReflectUtils.defineClass("java.lang.String");
        Assert.assertTrue(stringClazz.isPresent());
    }

    @Test
    public void testInvokeMethod() {
        String test = "   test  ";
        final Optional<Object> trim = ReflectUtils.invokeTargetMethod(test, "trim", null, null);
        Assert.assertTrue(trim.isPresent());
        Assert.assertEquals(trim.get(), test.trim());
        final TestMethod testMethod = new TestMethod();
        final Optional<Object> privateMethod = ReflectUtils.invokeTargetMethod(testMethod, "privateMethod", null, null);
        Assert.assertTrue(privateMethod.isPresent());
        Assert.assertEquals(privateMethod.get(), "private");
        String name = "Toy";
        final Optional<Object> hello = ReflectUtils
                .invokeTargetMethod(testMethod, "hello", new Class[]{String.class}, new Object[]{name});
        Assert.assertTrue(hello.isPresent());
        Assert.assertEquals(hello.get(), "hello " + name);
    }

    static class TestMethod {
        private String privateMethod() {
            return "private";
        }

        protected String hello(String name) {
            return "hello " + name;
        }
    }
}
