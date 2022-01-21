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

package com.huawei.flowcontrol.common.handler.retry;

import com.huawei.flowcontrol.common.adapte.cse.resolver.RetryResolver;
import com.huawei.flowcontrol.common.adapte.cse.rule.RetryRule;
import com.huawei.flowcontrol.common.handler.AbstractRequestHandler;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 重试处理器，创建processor
 *
 * @author zhouss
 * @since 2022-01-26
 */
public class RetryHandler extends AbstractRequestHandler<RetryProcessor, RetryRule> {
    @Override
    protected RetryProcessor createProcessor(String businessName, RetryRule rule) {
        return new RetryProcessor(rule, RetryContext.INSTANCE.getRetry(), getIntervalFunction(rule));
    }

    private Function<Integer, Long> getIntervalFunction(RetryRule rule) {
        return attempt -> {
            if (RetryRule.STRATEGY_RANDOM_BACKOFF.equals(rule.getRetryStrategy())) {
                // init * multiplier^n
                final List<Long> iterate = Stream.iterate(rule.getParsedInitialInterval(),
                        dl -> (long) rule.getMultiplier() * dl).limit(attempt).collect(Collectors.toList());
                return iterate.get(attempt - 1);
            }
            return rule.getParsedWaitDuration();
        };
    }

    @Override
    protected String configKey() {
        return RetryResolver.CONFIG_KEY;
    }
}
