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

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.service.xds.entity.FractionalPercent;
import io.sermant.core.service.xds.entity.XdsAbort;
import io.sermant.core.service.xds.entity.XdsDelay;
import io.sermant.core.service.xds.entity.XdsHttpFault;
import io.sermant.core.utils.StringUtils;
import io.sermant.flowcontrol.common.config.CommonConst;
import io.sermant.flowcontrol.common.core.rule.fault.FaultException;
import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.common.entity.RequestEntity;
import io.sermant.flowcontrol.common.entity.RequestEntity.RequestType;
import io.sermant.flowcontrol.common.util.RandomUtil;
import io.sermant.flowcontrol.common.xds.handler.XdsHandler;
import io.sermant.flowcontrol.res4j.chain.AbstractXdsChainHandler;
import io.sermant.flowcontrol.res4j.chain.HandlerConstants;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Current limiting request handler
 *
 * @author zhp
 * @since 2024-12-05
 */
public class XdsFaultRequestHandler extends AbstractXdsChainHandler {
    private static final String MESSAGE = "Request has been aborted by fault-ThrowException";

    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public void onBefore(RequestEntity requestEntity, FlowControlScenario scenarioInfo) {
        Optional<XdsHttpFault> xdsHttpFaultOptional = XdsHandler.INSTANCE.
                getHttpFault(scenarioInfo.getServiceName(), scenarioInfo.getRouteName());
        if (!xdsHttpFaultOptional.isPresent()) {
            super.onBefore(requestEntity, scenarioInfo);
            return;
        }
        XdsHttpFault xdsHttpFault = xdsHttpFaultOptional.get();
        if (xdsHttpFault.getAbort() == null && xdsHttpFault.getDelay() == null) {
            super.onBefore(requestEntity, scenarioInfo);
            return;
        }
        executeAbort(xdsHttpFault.getAbort());
        executeDelay(xdsHttpFault.getDelay());
        super.onBefore(requestEntity, scenarioInfo);
    }

    private void executeAbort(XdsAbort xdsAbort) {
        if (xdsAbort.getPercentage() == null) {
            return;
        }
        FractionalPercent fractionalPercent = xdsAbort.getPercentage();
        if (fractionalPercent.getNumerator() <= 0 || fractionalPercent.getDenominator() <= 0) {
            return;
        }
        int randomNum = RandomUtil.randomInt(fractionalPercent.getDenominator());
        if (randomNum < fractionalPercent.getNumerator()) {
            int status = xdsAbort.getHttpStatus() > 0 ? xdsAbort.getHttpStatus() : CommonConst.INTERVAL_SERVER_ERROR;
            throw new FaultException(status, MESSAGE, null);
        }
    }

    private void executeDelay(XdsDelay delay) {
        if (delay.getFixedDelay() <= 0 || delay.getPercentage() == null) {
            return;
        }
        FractionalPercent fractionalPercent = delay.getPercentage();
        if (fractionalPercent.getNumerator() <= 0 || fractionalPercent.getDenominator() <= 0) {
            return;
        }
        int randomNum = RandomUtil.randomInt(fractionalPercent.getDenominator());
        if (randomNum >= fractionalPercent.getNumerator()) {
            return;
        }
        LOGGER.log(Level.FINE, "Start delay request by delay fault, delay time is {0}ms", delay.getFixedDelay());
        try {
            Thread.sleep(delay.getFixedDelay());
        } catch (InterruptedException ignored) {
            // ignored
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
    protected boolean isSkip(RequestEntity requestEntity, FlowControlScenario scenarioInfo) {
        return scenarioInfo == null || StringUtils.isEmpty(scenarioInfo.getServiceName())
                || StringUtils.isEmpty(scenarioInfo.getRouteName());
    }

    @Override
    protected RequestType direct() {
        return RequestType.CLIENT;
    }

    @Override
    public int getOrder() {
        return HandlerConstants.XDS_FAULT_ORDER;
    }
}
