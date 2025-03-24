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

package com.example.sermant.agentcore.test.application.tests.enhancement;

import com.example.sermant.agentcore.test.application.results.EnhanceResults;

/**
 * Test enhancement capabilities
 *
 * @author luanwenfei
 * @since 2023-10-18
 */
public class EnhancementTest {
    private static String staticField = "staticField";

    private String memberField = "memberField";

    /**
     * Test enhancement capabilities
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
     * Test method skip
     */
    private boolean testSkipFunction() {
        EnhanceResults.SKIP_METHOD.setResult(false);
        return false;
    }

    /**
     * Test set property value
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
     * Test set arguments
     */
    private void testSetArguments(String arg) {
        if ("argSetBySermant".equals(arg)) {
            EnhanceResults.MODIFY_ARGUMENTS.setResult(true);
        }
    }
}
