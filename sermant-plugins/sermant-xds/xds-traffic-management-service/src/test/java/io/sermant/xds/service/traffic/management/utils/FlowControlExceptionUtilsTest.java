/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.sermant.xds.service.traffic.management.utils;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.sermant.xds.common.constant.CommonConst;
import io.sermant.xds.common.entity.FlowControlResult;
import io.sermant.xds.service.traffic.management.exception.FaultException;

import org.junit.Assert;
import org.junit.Test;

/**
 * rest4j test
 *
 * @author zhouss
 * @since 2022-03-03
 */
public class FlowControlExceptionUtilsTest {
    private static final String RULE_NAME = "test";

    /**
     * test exception handling
     */
    @Test
    public void testExceptionHandler() {
        final FlowControlResult flowControlResult = new FlowControlResult();
        FlowControlExceptionUtils.handleException(
                new FaultException(CommonConst.INTERVAL_SERVER_ERROR, "aborted by fault"),
                flowControlResult);
        Assert.assertEquals("aborted by fault", flowControlResult.getResponse().getMsg());
    }
}
