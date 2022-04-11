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
import com.huawei.flowcontrol.common.handler.retry.Retry;

import java.util.function.Predicate;

/**
 * 重试Predicate创建
 *
 * @author zhouss
 * @since 2022-04-11
 */
public interface RetryPredicateCreator {
    /**
     * 创建异常Predicate
     *
     * @param retryExceptions 重试异常集合
     * @return Predicate
     */
    Predicate<Throwable> createExceptionPredicate(Class<? extends Throwable>[] retryExceptions);

    /**
     * 创建重试结果Predicate
     *
     * @param retry 重试器
     * @param rule  重试规则
     * @return Predicate
     */
    Predicate<Object> createResultPredicate(Retry retry, RetryRule rule);
}
