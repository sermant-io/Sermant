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

package com.huaweicloud.agentcore.test.application.tests.enhancement;

import com.huaweicloud.agentcore.test.application.results.EnhanceResults;

/**
 * 测试增强能力
 *
 * @author luanwenfei
 * @since 2023-10-18
 */
public class EnhancementTest {
    private static String staticField = "staticField";

    private String memberField = "memberField";

    /**
     * 测试增强能力
     */
    public void testEnhancement() {
        EnhanceResults.SKIP_METHOD.setResult(true);
        if (testSkipFunction()) {
            EnhanceResults.MODIFY_RESULT.setResult(true);
        }
        testSetFiledFunction();
        testSetArguments("arg");
    }

    /**
     * 测试方法跳过
     */
    private boolean testSkipFunction() {
        EnhanceResults.SKIP_METHOD.setResult(false);
        return false;
    }

    /**
     * 测试设置属性值
     */
    private void testSetFiledFunction() {
        if ("staticFieldSetBySermant".equals(staticField)) {
            EnhanceResults.MODIFY_STATIC_FIELDS.setResult(true);
        }
        if ("memberFieldSetBySermant".equals(memberField)) {
            EnhanceResults.MODIFY_MEMBER_FIELDS.setResult(true);
        }
    }

    /**
     * 测试设置入参
     */
    private void testSetArguments(String arg) {
        if ("argSetBySermant".equals(arg)) {
            EnhanceResults.MODIFY_ARGUMENTS.setResult(true);
        }
    }
}
