/*
 * Copyright (C) 2022-2025 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.xds.traffic.management.handler;

import io.sermant.xds.common.flowcontrol.retry.Retry;

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
     * @param retry retry
     * @return Predicate
     */
    Predicate<Throwable> createExceptionPredicate(Retry retry);

    /**
     * create retry result predicate
     *
     * @param retry retry
     * @return Predicate
     */
    Predicate<Object> createResultPredicate(Retry retry);
}
