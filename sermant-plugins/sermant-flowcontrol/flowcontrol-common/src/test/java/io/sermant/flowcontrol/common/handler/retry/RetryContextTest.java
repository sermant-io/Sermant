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

package io.sermant.flowcontrol.common.handler.retry;

import io.sermant.flowcontrol.common.core.rule.RetryRule;

import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

/**
 * retry test
 *
 * @author zhouss
 * @since 2022-08-29
 */
public class RetryContextTest {
    /**
     * mark test
     */
    @Test
    public void testMark() {
        RetryContext.INSTANCE.markRetry(buildRetry());
        Assert.assertTrue(RetryContext.INSTANCE.isMarkedRetry());
        RetryContext.INSTANCE.remove();
        Assert.assertFalse(RetryContext.INSTANCE.isMarkedRetry());
    }

    /**
     * test retry strategy
     */
    @Test
    public void testRetryPolicy() {
        int maxRetry = 8;
        final RetryRule retryRule = new RetryRule();
        retryRule.setRetryOnSame(maxRetry);
        retryRule.setFailAfterMaxAttempts(true);
        RetryContext.INSTANCE.buildRetryPolicy(retryRule);
        Assert.assertFalse(RetryContext.INSTANCE.isPolicyNeedRetry());

        // start simulated retry
        final Object instance = new Object();
        for (int i = 0; i < maxRetry; i++) {
            muteRetry(instance);
        }

        // If the number of retries exceeds the maximum, retry is complete
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
