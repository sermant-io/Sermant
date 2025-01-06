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

import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.flowcontrol.common.config.XdsFlowControlConfig;
import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.common.entity.RequestEntity;
import io.sermant.flowcontrol.common.entity.RequestEntity.RequestType;

/**
 * abstract Xds handler chain, the order of execution is as follows:
 * <p>onBefore -> onThrow(executed when an exception exists) -> onResult</p>
 *
 * @author zhp
 * @since 2024-12-28
 */
public abstract class AbstractXdsChainHandler implements XdsRequestHandler, Comparable<AbstractXdsChainHandler> {
    protected static final XdsFlowControlConfig XDS_FLOW_CONTROL_CONFIG =
            PluginConfigManager.getPluginConfig(XdsFlowControlConfig.class);

    private AbstractXdsChainHandler next;

    @Override
    public void onBefore(RequestEntity requestEntity, FlowControlScenario flowControlScenario) {
        AbstractXdsChainHandler cur = getNextHandler(requestEntity, flowControlScenario);
        if (cur != null) {
            cur.onBefore(requestEntity, flowControlScenario);
        }
    }

    @Override
    public void onThrow(RequestEntity requestEntity, FlowControlScenario flowControlScenario, Throwable throwable) {
        AbstractXdsChainHandler cur = getNextHandler(requestEntity, flowControlScenario);
        if (cur != null) {
            cur.onThrow(requestEntity, flowControlScenario, throwable);
        }
    }

    @Override
    public void onAfter(RequestEntity requestEntity, FlowControlScenario flowControlScenario, Object result) {
        AbstractXdsChainHandler cur = getNextHandler(requestEntity, flowControlScenario);
        if (cur != null) {
            cur.onAfter(requestEntity, flowControlScenario, result);
        }
    }

    private AbstractXdsChainHandler getNextHandler(RequestEntity entity, FlowControlScenario flowControlScenario) {
        AbstractXdsChainHandler handler = next;
        while (handler != null) {
            if (!isNeedSkip(handler, entity, flowControlScenario)) {
                break;
            }
            handler = handler.getNext();
        }
        return handler;
    }

    private boolean isNeedSkip(AbstractXdsChainHandler handler, RequestEntity requestEntity,
            FlowControlScenario scenario) {
        final RequestType direct = handler.direct();
        if (direct != RequestType.BOTH && requestEntity.getRequestType() != direct) {
            return true;
        }
        return handler.isSkip(requestEntity, scenario);
    }

    /**
     * request direction, both are processed by default, In actual processing, the request direction determines whether
     * it needs to be processed by the current handler
     *
     * @return RequestType
     */
    protected RequestType direct() {
        return RequestType.BOTH;
    }

    /**
     * whether to skip the current handler
     *
     * @param requestEntity request-information
     * @param flowControlScenario matched scenario information
     * @return skip or not
     */
    protected boolean isSkip(RequestEntity requestEntity, FlowControlScenario flowControlScenario) {
        return !XDS_FLOW_CONTROL_CONFIG.isEnable();
    }

    @Override
    public int compareTo(AbstractXdsChainHandler handler) {
        return getOrder() - handler.getOrder();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public void setNext(AbstractXdsChainHandler handler) {
        this.next = handler;
    }

    public AbstractXdsChainHandler getNext() {
        return next;
    }
}
