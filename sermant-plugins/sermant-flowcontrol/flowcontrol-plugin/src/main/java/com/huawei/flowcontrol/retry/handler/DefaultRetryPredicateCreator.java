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

package com.huawei.flowcontrol.retry.handler;

import com.huawei.flowcontrol.common.adapte.cse.rule.RetryRule;
import com.huawei.flowcontrol.common.exception.InvokerWrapperException;
import com.huawei.flowcontrol.common.handler.retry.Retry;
import com.huawei.flowcontrol.common.util.ReflectUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 默认异常predicate创建器
 *
 * @author zhouss
 * @since 2022-04-11
 */
public class DefaultRetryPredicateCreator implements RetryPredicateCreator {
    private static final String ALIBABA_GENERIC_SERVICE = "com.alibaba.dubbo.rpc.service.GenericException";

    private static final String APACHE_GENERIC_SERVICE = "org.apache.dubbo.rpc.service.GenericService";

    @Override
    public Predicate<Throwable> createExceptionPredicate(Class<? extends Throwable>[] retryExceptions) {
        return Arrays.stream(retryExceptions).distinct().map(this::createExceptionPredicate).reduce(Predicate::or)
            .orElseGet(() -> throwable -> true);
    }

    private Predicate<Throwable> createExceptionPredicate(Class<? extends Throwable> retryClass) {
        return (Throwable ex) -> {
            if (retryClass.isAssignableFrom(getRealExceptionClass(ex))) {
                return true;
            }
            final Optional<String> realExceptionClassName = getRealExceptionClassName(ex);
            return realExceptionClassName.isPresent() && retryClass.getName().equals(realExceptionClassName.get());
        };
    }

    /**
     * 针对包装异常处理
     * <p></p>
     * 当前仅支持GenericException
     *
     * @param ex 业务异常
     * @return 异常名称
     */
    private Optional<String> getRealExceptionClassName(Throwable ex) {
        String mayBeRealClassName = null;
        if (isGenericException(ex.getClass().getName())) {
            final Optional<Object> getExceptionClass = ReflectUtils
                .invokeTargetMethod(ex, "getExceptionClass", null, null);
            if (getExceptionClass.isPresent()) {
                mayBeRealClassName = (String) getExceptionClass.get();
            }
        }
        return Optional.ofNullable(mayBeRealClassName);
    }

    private boolean isGenericException(String className) {
        return ALIBABA_GENERIC_SERVICE.equals(className) || APACHE_GENERIC_SERVICE.equals(className);
    }

    private Class<? extends Throwable> getRealExceptionClass(Throwable ex) {
        if (ex instanceof InvokerWrapperException) {
            // 判断是否是目标包装异常
            InvokerWrapperException invokerWrapperException = (InvokerWrapperException) ex;
            if (invokerWrapperException.getRealException() != null) {
                return invokerWrapperException.getRealException().getClass();
            }
        }
        return ex.getClass();
    }

    @Override
    public Predicate<Object> createResultPredicate(Retry retry, RetryRule rule) {
        return result -> retry.needRetry(new HashSet<>(rule.getRetryOnResponseStatus()), result);
    }
}
