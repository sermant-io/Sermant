/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.example.sermant.agentcore.test.application.tests.classmatch;

/**
 * 测试类匹配相关功能
 *
 * @author luanwenfei
 * @since 2023-10-18
 */
public class ClassMatchersTest {
    /**
     * 测试测试类匹配相关功能
     */
    public void testClassMatchers() {
        AnnotationTest.staticFunction(false);
        AnnotationsTest.staticFunction(false);
        SuperTypeTest.staticFunction(false);
        SuperTypesTest.staticFunction(false);
        PrefixNameTest.staticFunction(false);
        NameInfixTest.staticFunction(false);
        NameTestSuffix.staticFunction(false);
    }
}
