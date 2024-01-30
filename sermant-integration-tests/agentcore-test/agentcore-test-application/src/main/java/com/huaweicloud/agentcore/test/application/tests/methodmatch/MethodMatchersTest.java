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

package com.huaweicloud.agentcore.test.application.tests.methodmatch;

import com.huaweicloud.agentcore.test.application.common.TestAnnotationA;
import com.huaweicloud.agentcore.test.application.common.TestAnnotationB;
import com.huaweicloud.agentcore.test.application.results.MethodMatchResults;

/**
 * 测试方法匹配相关功能
 *
 * @author luanwenfei
 * @since 2023-10-18
 */
public class MethodMatchersTest {
    /**
     * 测试构造方法
     *
     * @param enhanceFlag 增强回执
     */
    public MethodMatchersTest(boolean enhanceFlag) {
        if (enhanceFlag) {
            MethodMatchResults.MATCHER_CLASS_BY_CLASS_NAME_EXACTLY.setResult(true);
            MethodMatchResults.MATCHER_CONSTRUCTOR.setResult(true);
        }
    }

    /**
     * 测试方法匹配功能
     */
    public void testMethodMatchers() {
        MethodMatchersTest.staticMethod(false);
        exactNameMethod(false);
        prefixNameMethod(false);
        nameInfixMethod(false);
        methodNameSuffix(false);
        returnType(false);
        argumentsCount(false, 1, "A");
        argumentsType(false, false);
        byAnnotation(false);
        byAnnotations(false);
    }

    /**
     * 测试静态方法
     *
     * @param enhanceFlag 增强回执
     */
    public static void staticMethod(boolean enhanceFlag) {
        if (enhanceFlag) {
            MethodMatchResults.MATCHER_STATIC_METHODS.setResult(true);
        }
    }

    /**
     * 测试精确方法名匹配
     *
     * @param enhanceFlag 增强回执
     */
    private void exactNameMethod(boolean enhanceFlag) {
        if (enhanceFlag) {
            MethodMatchResults.MATCHER_METHOD_BY_METHOD_NAME_EXACTLY.setResult(true);
        }
    }

    /**
     * 测试前缀方法名匹配
     *
     * @param enhanceFlag 增强回执
     */
    private void prefixNameMethod(boolean enhanceFlag) {
        if (enhanceFlag) {
            MethodMatchResults.MATCHER_METHOD_BY_METHOD_NAME_PREFIX.setResult(true);
        }
    }

    /**
     * 测试中缀方法名匹配
     *
     * @param enhanceFlag 增强回执
     */
    private void nameInfixMethod(boolean enhanceFlag) {
        if (enhanceFlag) {
            MethodMatchResults.MATCHER_METHOD_BY_METHOD_NAME_INFIX.setResult(true);
        }
    }

    /**
     * 测试后缀方法名匹配
     *
     * @param enhanceFlag 增强回执
     */
    private void methodNameSuffix(boolean enhanceFlag) {
        if (enhanceFlag) {
            MethodMatchResults.MATCHER_METHOD_BY_METHOD_NAME_SUFFIX.setResult(true);
        }
    }

    /**
     * 测试方法返回类型匹配
     *
     * @param enhanceFlag 增强回执
     */
    private boolean returnType(boolean enhanceFlag) {
        if (enhanceFlag) {
            MethodMatchResults.MATCHER_METHOD_BY_RETURN_TYPE.setResult(true);
        }
        return false;
    }

    /**
     * 测试方法入参数量匹配
     *
     * @param enhanceFlag 增强回执
     * @param argA 测试入参A
     * @param argB 测试入参B
     */
    private void argumentsCount(boolean enhanceFlag, int argA, String argB) {
        if (enhanceFlag) {
            MethodMatchResults.MATCHER_METHOD_BY_ARGUMENTS_COUNT.setResult(true);
        }
    }

    /**
     * 测试方法入参类型匹配
     *
     * @param enhanceFlag 增强回执
     * @param arg 测试入参
     */
    private void argumentsType(boolean enhanceFlag, boolean arg) {
        if (enhanceFlag) {
            MethodMatchResults.MATCHER_METHOD_BY_ARGUMENTS_TYPE.setResult(true);
        }
    }

    /**
     * 测试单一注解方法
     *
     * @param enhanceFlag 增强回执
     */
    @TestAnnotationA
    private void byAnnotation(boolean enhanceFlag) {
        if (enhanceFlag) {
            MethodMatchResults.MATCHER_METHOD_BY_ANNOTATION.setResult(true);
        }
    }

    /**
     * 测试多注解方法
     *
     * @param enhanceFlag 增强回执
     */
    @TestAnnotationA
    @TestAnnotationB
    private void byAnnotations(boolean enhanceFlag) {
        if (enhanceFlag) {
            MethodMatchResults.MATCHER_METHOD_BY_ANNOTATIONS.setResult(true);
        }
    }
}
