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

package io.sermant.flowcontrol.retry.handler;

import feign.Response;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.flowcontrol.common.core.rule.RetryRule;
import io.sermant.flowcontrol.common.exception.InvokerWrapperException;
import io.sermant.flowcontrol.common.handler.retry.Retry;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * constructor test
 *
 * @author zhouss
 * @since 2022-08-31
 */
public class DefaultRetryPredicateCreatorTest {
    @Test
    public void test() {
        final DefaultRetryPredicateCreator creator = new DefaultRetryPredicateCreator();
        final Predicate exceptionPredicate = creator
                .createExceptionPredicate(new Class[]{IllegalArgumentException.class, ArithmeticException.class});
        Assert.assertTrue(exceptionPredicate.test(new IllegalArgumentException()));
        Assert.assertTrue(exceptionPredicate.test(new ArithmeticException()));
        Assert.assertTrue(exceptionPredicate.test(new InvokerWrapperException(new IllegalArgumentException())));
        final Predicate<Object> resultPredicate = creator.createResultPredicate(new TestRetry(), new RetryRule());
        final Response mock = Mockito.mock(Response.class);
        Mockito.when(mock.status()).thenReturn(HttpStatus.SERVICE_UNAVAILABLE.value());
        Assert.assertTrue(resultPredicate.test(mock));
        Mockito.when(mock.status()).thenReturn(HttpStatus.OK.value());
        Assert.assertFalse(resultPredicate.test(mock));
    }

    static class TestRetry implements Retry {

        @Override
        public boolean needRetry(Set<String> statusList, Object result) {
            final Optional<Object> status = ReflectUtils.invokeMethod(result, "status", null, null);
            if (status.isPresent()) {
                final Object code = status.get();
                return statusList.contains(String.valueOf(code));
            }
            return false;
        }

        @Override
        public Class<? extends Throwable>[] retryExceptions() {
            return new Class[0];
        }

        @Override
        public RetryFramework retryType() {
            return RetryFramework.SPRING_CLOUD;
        }

        @Override
        public Optional<String> getCode(Object result) {
            return Optional.empty();
        }

        @Override
        public Optional<Set<String>> getHeaderNames(Object result) {
            return Optional.empty();
        }
    }
}
