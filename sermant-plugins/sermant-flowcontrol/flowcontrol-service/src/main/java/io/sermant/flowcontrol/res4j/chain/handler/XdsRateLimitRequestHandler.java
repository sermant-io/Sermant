/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.flowcontrol.res4j.chain.handler;

import io.sermant.core.service.xds.entity.FractionalPercent;
import io.sermant.core.service.xds.entity.XdsRateLimit;
import io.sermant.core.service.xds.entity.XdsTokenBucket;
import io.sermant.core.utils.StringUtils;
import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.common.entity.RequestEntity;
import io.sermant.flowcontrol.common.entity.RequestEntity.RequestType;
import io.sermant.flowcontrol.common.util.RandomUtil;
import io.sermant.flowcontrol.common.xds.handler.XdsHandler;
import io.sermant.flowcontrol.common.xds.ratelimit.XdsRateLimitManager;
import io.sermant.flowcontrol.res4j.chain.AbstractXdsChainHandler;
import io.sermant.flowcontrol.res4j.chain.HandlerConstants;
import io.sermant.flowcontrol.res4j.exceptions.RateLimitException;

import java.util.Optional;

/**
 * Current limiting request handler
 *
 * @author zhp
 * @since 2024-12-05
 */
public class XdsRateLimitRequestHandler extends AbstractXdsChainHandler {
    @Override
    public void onBefore(RequestEntity requestEntity, FlowControlScenario scenarioInfo) {
        handleRateLimit(scenarioInfo);
        super.onBefore(requestEntity, scenarioInfo);
    }

    private void handleRateLimit(FlowControlScenario scenarioInfo) {
        Optional<XdsRateLimit> xdsRateLimitOptional = XdsHandler.INSTANCE.getRateLimit(
                scenarioInfo.getServiceName(), scenarioInfo.getRouteName(), scenarioInfo.getClusterName());
        if (!xdsRateLimitOptional.isPresent()) {
            return;
        }
        XdsRateLimit xdsRateLimit = xdsRateLimitOptional.get();
        XdsTokenBucket tokenBucket = xdsRateLimit.getTokenBucket();
        if (tokenBucket == null || xdsRateLimit.getPercent() == null
                || tokenBucket.getMaxTokens() <= 0 || tokenBucket.getFillInterval() <= 0) {
            return;
        }
        FractionalPercent fractionalPercent = xdsRateLimit.getPercent();
        if (fractionalPercent.getNumerator() <= 0 || fractionalPercent.getDenominator() <= 0) {
            return;
        }
        int randomNum = RandomUtil.randomInt(fractionalPercent.getDenominator());
        if (randomNum >= fractionalPercent.getNumerator()) {
            return;
        }
        if (!XdsRateLimitManager.fillAndConsumeToken(scenarioInfo.getServiceName(), scenarioInfo.getRouteName(),
                tokenBucket)) {
            throw new RateLimitException(xdsRateLimit.getResponseHeaderOption());
        }
    }

    @Override
    public void onThrow(RequestEntity requestEntity, FlowControlScenario scenarioInfo, Throwable throwable) {
        super.onThrow(requestEntity, scenarioInfo, throwable);
    }

    @Override
    public void onAfter(RequestEntity requestEntity, FlowControlScenario scenarioInfo, Object result) {
        super.onAfter(requestEntity, scenarioInfo, result);
    }

    @Override
    public int getOrder() {
        return HandlerConstants.XDS_RATE_LIMIT_ORDER;
    }

    @Override
    protected boolean isSkip(RequestEntity requestEntity, FlowControlScenario scenarioInfo) {
        return scenarioInfo == null || StringUtils.isEmpty(scenarioInfo.getServiceName())
                || StringUtils.isEmpty(scenarioInfo.getRouteName());
    }

    @Override
    protected RequestType direct() {
        return RequestType.SERVER;
    }
}
