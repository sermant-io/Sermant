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

package com.example.sermant.agentcore.test.application.tests.methodmatch;

import com.example.sermant.agentcore.test.application.common.TestAnnotationA;
import com.example.sermant.agentcore.test.application.common.TestAnnotationB;
import com.example.sermant.agentcore.test.application.results.MethodMatchResults;

/**
 * Test method matching functions
 *
 * @author luanwenfei
 * @since 2023-10-18
 */
public class MethodMatchersTest {
    /**
     * Test constructor
     *
     * @param enhanceFlag enhance result
     */
    public MethodMatchersTest(boolean enhanceFlag) {
        if (enhanceFlag) {
            MethodMatchResults.MATCHER_CLASS_BY_CLASS_NAME_EXACTLY.setResult(true);
            MethodMatchResults.MATCHER_CONSTRUCTOR.setResult(true);
        }
    }

    /**
     * Test method matching functions
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
     * Test static method matching
     *
     * @param enhanceFlag enhance result
     */
    public static void staticMethod(boolean enhanceFlag) {
        if (enhanceFlag) {
            MethodMatchResults.MATCHER_STATIC_METHODS.setResult(true);
        }
    }

    /**
     * Test exact method name matching
     *
     * @param enhanceFlag enhance result
     */
    private void exactNameMethod(boolean enhanceFlag) {
        if (enhanceFlag) {
            MethodMatchResults.MATCHER_METHOD_BY_METHOD_NAME_EXACTLY.setResult(true);
        }
    }

    /**
     * Test prefix method name matching
     *
     * @param enhanceFlag enhance result
     */
    private void prefixNameMethod(boolean enhanceFlag) {
        if (enhanceFlag) {
            MethodMatchResults.MATCHER_METHOD_BY_METHOD_NAME_PREFIX.setResult(true);
        }
    }

    /**
     * Test infix method name matching
     *
     * @param enhanceFlag enhance result
     */
    private void nameInfixMethod(boolean enhanceFlag) {
        if (enhanceFlag) {
            MethodMatchResults.MATCHER_METHOD_BY_METHOD_NAME_INFIX.setResult(true);
        }
    }

    /**
     * Test suffix method name matching
     *
     * @param enhanceFlag enhance result
     */
    private void methodNameSuffix(boolean enhanceFlag) {
        if (enhanceFlag) {
            MethodMatchResults.MATCHER_METHOD_BY_METHOD_NAME_SUFFIX.setResult(true);
        }
    }

    /**
     * Test method return type matching
     *
     * @param enhanceFlag enhance result
     */
    private boolean returnType(boolean enhanceFlag) {
        if (enhanceFlag) {
            MethodMatchResults.MATCHER_METHOD_BY_RETURN_TYPE.setResult(true);
        }
        return false;
    }

    /**
     * Test method argument count matching
     *
     * @param enhanceFlag enhance result
     * @param argA test argument A
     * @param argB test argument B
     */
    private void argumentsCount(boolean enhanceFlag, int argA, String argB) {
        if (enhanceFlag) {
            MethodMatchResults.MATCHER_METHOD_BY_ARGUMENTS_COUNT.setResult(true);
        }
    }

    /**
     * Test method argument type matching
     *
     * @param enhanceFlag enhance result
     * @param arg test argument
     */
    private void argumentsType(boolean enhanceFlag, boolean arg) {
        if (enhanceFlag) {
            MethodMatchResults.MATCHER_METHOD_BY_ARGUMENTS_TYPE.setResult(true);
        }
    }

    /**
     * Test single annotation method
     *
     * @param enhanceFlag enhance result
     */
    @TestAnnotationA
    private void byAnnotation(boolean enhanceFlag) {
        if (enhanceFlag) {
            MethodMatchResults.MATCHER_METHOD_BY_ANNOTATION.setResult(true);
        }
    }

    /**
     * Test multiple annotation method
     *
     * @param enhanceFlag enhance result
     */
    @TestAnnotationA
    @TestAnnotationB
    private void byAnnotations(boolean enhanceFlag) {
        if (enhanceFlag) {
            MethodMatchResults.MATCHER_METHOD_BY_ANNOTATIONS.setResult(true);
        }
    }
}
