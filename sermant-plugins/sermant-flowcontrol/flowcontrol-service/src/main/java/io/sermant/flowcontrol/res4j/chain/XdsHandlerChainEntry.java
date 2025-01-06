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

package io.sermant.flowcontrol.res4j.chain;

import io.sermant.core.common.LoggerFactory;
import io.sermant.flowcontrol.common.entity.FlowControlResult;
import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.common.entity.RequestEntity;
import io.sermant.flowcontrol.res4j.util.FlowControlExceptionUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * request chain entry class
 *
 * @author zhp
 * @since 2024-12-28
 */
public enum XdsHandlerChainEntry {
    /**
     * singleton
     */
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * HandlerChain
     */
    private final XdsHandlerChain chain = HandlerChainBuilder.INSTANCE.buildXdsHandlerChain();

    /**
     * pre-method
     *
     * @param requestEntity request body
     * @param flowControlResult flow control result
     */
    public void onBefore(RequestEntity requestEntity, FlowControlResult flowControlResult) {
        try {
            chain.onBefore(requestEntity, null);
        } catch (Exception ex) {
            flowControlResult.setRequestType(requestEntity.getRequestType());
            FlowControlExceptionUtils.handleException(ex, flowControlResult);
            LOGGER.log(Level.FINE, ex, ex::getMessage);
        }
    }

    /**
     * postset method
     *
     * @param requestEntity request body
     * @param result execution result
     * @param flowControlScenario Scenario information for flow control
     */
    public void onAfter(RequestEntity requestEntity, Object result, FlowControlScenario flowControlScenario) {
        chain.onAfter(requestEntity, flowControlScenario, result);
    }

    /**
     * exception method
     *
     * @param requestEntity request body
     * @param throwable exception message
     * @param flowControlScenario Scenario information for flow control
     */
    public void onThrow(RequestEntity requestEntity, Throwable throwable, FlowControlScenario flowControlScenario) {
        chain.onThrow(requestEntity, flowControlScenario, throwable);
    }
}
