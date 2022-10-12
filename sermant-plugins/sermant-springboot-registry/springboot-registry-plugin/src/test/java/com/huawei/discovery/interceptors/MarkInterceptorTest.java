/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.discovery.interceptors;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * mark测试
 *
 * @author zhouss
 * @since 2022-10-12
 */
public class MarkInterceptorTest {
    @Test
    public void test() throws Exception {
        final MarkInterceptor markInterceptor = new MarkInterceptor() {
            @Override
            protected ExecuteContext doBefore(ExecuteContext context) {
                return context;
            }

            @Override
            public ExecuteContext after(ExecuteContext context) {
                return context;
            }

            @Override
            public ExecuteContext onThrow(ExecuteContext context) {
                return context;
            }
        };
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final ExecuteContext executeContext = buildContext(new Object());
        final ExecuteContext before = markInterceptor.before(executeContext);
        final Thread thread = new Thread(() -> {
            try {
                markInterceptor.before(executeContext);
                countDownLatch.countDown();
            } catch (Exception exception) {
                // ignored
            }
        });
        thread.start();
        countDownLatch.await();
        Assert.assertEquals(before, executeContext);
    }

    private ExecuteContext buildContext(Object target) throws NoSuchMethodException {
        return ExecuteContext.forMemberMethod(target, String.class.getDeclaredMethod("trim"), null,
                null, null);
    }
}
