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

package com.huawei.flowcontrol.common.handler.retry;

import com.huawei.flowcontrol.common.core.rule.RetryRule;

import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

/**
 * 重试测试
 *
 * @author zhouss
 * @since 2022-08-29
 */
public class RetryContextTest {
    /**
     * mark测试
     */
    @Test
    public void testMark() {
        RetryContext.INSTANCE.markRetry(buildRetry());
        Assert.assertTrue(RetryContext.INSTANCE.isMarkedRetry());
        RetryContext.INSTANCE.remove();
        Assert.assertFalse(RetryContext.INSTANCE.isMarkedRetry());
    }

    /**
     * 测试重试策略
     */
    @Test
    public void testRetryPolicy() {
        int maxRetry = 8;
        final RetryRule retryRule = new RetryRule();
        retryRule.setRetryOnSame(maxRetry);
        retryRule.setFailAfterMaxAttempts(true);
        RetryContext.INSTANCE.buildRetryPolicy(retryRule);
        Assert.assertFalse(RetryContext.INSTANCE.isPolicyNeedRetry());

        // 开始模拟重试
        final Object instance = new Object();
        for (int i = 0; i < maxRetry; i++) {
            muteRetry(instance);
        }

        // 超过最大重试次数则重试完成
        RetryContext.INSTANCE.updateServiceInstance(instance);
        Assert.assertFalse(RetryContext.INSTANCE.isPolicyNeedRetry());
        RetryContext.INSTANCE.remove();
    }

    private void muteRetry(Object instance) {
        RetryContext.INSTANCE.updateServiceInstance(instance);
        Assert.assertTrue(RetryContext.INSTANCE.isPolicyNeedRetry());
    }

    private Retry buildRetry() {
        return new Retry() {
            @Override
            public boolean needRetry(Set<String> statusList, Object result) {
                return false;
            }

            @Override
            public Class<? extends Throwable>[] retryExceptions() {
                return new Class[0];
            }

            @Override
            public RetryFramework retryType() {
                return RetryFramework.ALIBABA_DUBBO;
            }
        };
    }
}
