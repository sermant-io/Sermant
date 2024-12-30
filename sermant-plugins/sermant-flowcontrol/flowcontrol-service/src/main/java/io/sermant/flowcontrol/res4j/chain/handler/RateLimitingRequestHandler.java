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

package io.sermant.flowcontrol.res4j.chain.handler;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.res4j.chain.HandlerConstants;
import io.sermant.flowcontrol.res4j.chain.context.RequestContext;
import io.sermant.flowcontrol.res4j.handler.RateLimitingHandler;

import java.util.List;

/**
 * Current limiting request handler
 *
 * @author zhouss
 * @since 2022-07-05
 */
public class RateLimitingRequestHandler extends FlowControlHandler<RateLimiter> {
    private final RateLimitingHandler rateLimitingHandler = new RateLimitingHandler();

    @Override
    public void onBefore(RequestContext context, FlowControlScenario scenarioInfo) {
        final List<RateLimiter> handlers =
                rateLimitingHandler.createOrGetHandlers(scenarioInfo.getMatchedScenarioNames());
        if (!handlers.isEmpty()) {
            context.save(getContextName(), handlers);
            handlers.forEach(rateLimiter -> RateLimiter.waitForPermission(rateLimiter, 1));
        }
        super.onBefore(context, scenarioInfo);
    }

    @Override
    public void onThrow(RequestContext context, FlowControlScenario scenarioInfo, Throwable throwable) {
        final List<RateLimiter> rateLimiters = getHandlersFromCache(context.getSourceName(), getContextName());
        if (rateLimiters != null) {
            rateLimiters.forEach(rateLimiter -> rateLimiter.onError(throwable));
        }
        super.onThrow(context, scenarioInfo, throwable);
    }

    @Override
    public void onResult(RequestContext context, FlowControlScenario scenarioInfo, Object result) {
        try {
            final List<RateLimiter> rateLimiters = getHandlersFromCache(context.getSourceName(), getContextName());
            if (rateLimiters != null) {
                rateLimiters.forEach(rateLimiter -> rateLimiter.onResult(result));
            }
        } finally {
            context.remove(getContextName());
        }
        super.onResult(context, scenarioInfo, result);
    }

    @Override
    public int getOrder() {
        return HandlerConstants.RATE_LIMIT_ORDER;
    }
}
