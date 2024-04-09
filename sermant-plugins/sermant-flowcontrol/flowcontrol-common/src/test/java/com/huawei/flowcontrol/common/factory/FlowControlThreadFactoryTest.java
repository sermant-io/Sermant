/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.flowcontrol.common.factory;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * thread factory test
 *
 * @author zhouss
 * @since 2022-08-29
 */
public class FlowControlThreadFactoryTest {
    @Test
    public void test() {
        final String threadName = "thread-name";
        final ExecutorService executorService = Executors
                .newFixedThreadPool(1, new FlowControlThreadFactory(threadName));
        executorService.execute(() -> {
            Assert.assertEquals(threadName, Thread.currentThread().getName());
        });
        executorService.shutdown();
    }
}
