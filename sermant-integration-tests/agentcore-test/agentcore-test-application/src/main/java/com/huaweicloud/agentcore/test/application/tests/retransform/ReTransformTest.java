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

package com.huaweicloud.agentcore.test.application.tests.retransform;

import com.huaweicloud.agentcore.test.application.results.ReTransformResults;

import java.util.Map;

/**
 * 测试类的重转换能力
 *
 * @author luanwenfei
 * @since 2023-10-18
 */
public class ReTransformTest {
    private static final int COUNT = 999;

    private static final String NAME = "modifyName";

    private static boolean staticFlag;

    private static boolean constructorFlag;

    /**
     * 测试类的重转换能力
     */
    public void testReTransform() {
        staticFlag = false;
        constructorFlag = false;

        // 测试拦截Thread类的无参构造函数
        Thread thread = new Thread();
        ReTransformResults.ENHANCE_NO_ARGUMENT_CONSTRUCTOR.setResult(constructorFlag);

        // 测试静态方法
        Map<Thread, StackTraceElement[]> stackTraces = Thread.getAllStackTraces();
        ReTransformResults.ENHANCE_STATIC_FUNCTION.setResult(staticFlag);

        // 测试实例方法参数修改
        thread.setName("thread1");
        if (thread.getName().equals(NAME)) {
            ReTransformResults.ENHANCE_INSTANCE_FUNCTION.setResult(true);
        }

        // 测试静态方法skip
        if (Thread.activeCount() == COUNT) {
            ReTransformResults.ENHANCE_STATIC_FUNCTION_SKIP.setResult(true);
        }
    }

    public static void setStaticFlag(boolean flag) {
        staticFlag = flag;
    }

    public static void setConstructorFlag(boolean flag) {
        constructorFlag = flag;
    }
}
