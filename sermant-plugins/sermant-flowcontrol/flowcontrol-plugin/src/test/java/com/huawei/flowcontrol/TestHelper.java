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

package com.huawei.flowcontrol;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import java.util.Collections;

/**
 * test help class
 *
 * @author zhouss
 * @since 2022-08-30
 */
public class TestHelper {
    private TestHelper() {
    }

    /**
     * build the default context
     *
     * @return context
     * @throws NoSuchMethodException will not throw
     */
    public static ExecuteContext buildDefaultContext() throws NoSuchMethodException {
        return ExecuteContext.forMemberMethod(
                new TestHelper(),
                String.class.getMethod("trim"),
                new Object[0],
                Collections.emptyMap(),
                Collections.emptyMap()
        );
    }
}
