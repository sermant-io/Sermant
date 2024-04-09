/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.agentcore.test.application.tests.classmatch;

import com.huaweicloud.agentcore.test.application.common.TestAnnotationA;
import com.huaweicloud.agentcore.test.application.results.ClassMatchResults;

/**
 * 测试通过单个注解匹配类
 *
 * @author luanwenfei
 * @since 2023-10-18
 */
@TestAnnotationA
public class AnnotationTest {
    private AnnotationTest() {
    }

    /**
     * 测试单个注解匹配拦截
     *
     * @param enhanceFlag 增强回执
     */
    public static void staticFunction(boolean enhanceFlag) {
        if (enhanceFlag) {
            ClassMatchResults.MATCHER_CLASS_BY_ANNOTATION.setResult(true);
        }
    }
}
