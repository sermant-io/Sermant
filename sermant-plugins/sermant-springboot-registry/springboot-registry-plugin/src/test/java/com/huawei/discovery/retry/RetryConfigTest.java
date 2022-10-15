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

package com.huawei.discovery.retry;

import static org.junit.Assert.*;

import org.apache.http.conn.ConnectTimeoutException;
import org.junit.Assert;
import org.junit.Test;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 重试测试
 *
 * @author zhouss
 * @since 2022-10-14
 */
public class RetryConfigTest {
    private final List<Class<? extends Throwable>> retryEx = Arrays.asList(
            ConnectException.class,
            NoRouteToHostException.class);

    private final List<String> rawRetryEx = Arrays.asList("org.apache.http.conn.ConnectTimeoutException",
            "com.huawei.discovery.retry.RetryConfigTest$Ex");

    @Test
    public void testExPredicate() {
        final RetryConfig retryConfig = new RetryConfig(
                retryEx,
                rawRetryEx,
                result -> false,
                "test",
                1,
                1);
        Assert.assertTrue(retryConfig.getThrowablePredicate().test(new ConnectTimeoutException()));
        // 子类
        Assert.assertTrue(retryConfig.getThrowablePredicate().test(new MyEx()));
        Assert.assertTrue(retryConfig.getThrowablePredicate().test(new MyInter()));
    }

    static class MyEx extends ConnectTimeoutException {

    }

    interface Ex {
    }

    static class MyInter extends Exception implements Ex {

    }
}
