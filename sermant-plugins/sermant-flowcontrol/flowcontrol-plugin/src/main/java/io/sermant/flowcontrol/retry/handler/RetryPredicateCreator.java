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

package io.sermant.flowcontrol.retry.handler;

import io.sermant.flowcontrol.common.core.rule.RetryRule;
import io.sermant.flowcontrol.common.handler.retry.Retry;

import java.util.function.Predicate;

/**
 * retry predicate creation
 *
 * @author zhouss
 * @since 2022-04-11
 */
public interface RetryPredicateCreator {
    /**
     * Create exception Predicate
     *
     * @param retryExceptions retry exception set
     * @return Predicate
     */
    Predicate<Throwable> createExceptionPredicate(Class<? extends Throwable>[] retryExceptions);

    /**
     * create retry result predicate
     *
     * @param retry retry
     * @param rule retry rule
     * @return Predicate
     */
    Predicate<Object> createResultPredicate(Retry retry, RetryRule rule);
}
