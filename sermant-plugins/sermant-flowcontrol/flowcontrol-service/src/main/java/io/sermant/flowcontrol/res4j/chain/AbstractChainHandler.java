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

package io.sermant.flowcontrol.res4j.chain;

import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.flowcontrol.common.config.XdsFlowControlConfig;
import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.common.entity.RequestEntity.RequestType;
import io.sermant.flowcontrol.res4j.chain.context.RequestContext;

/**
 * abstract handler chain, the order of execution is as follows:
 * <p>onBefore -> onThrow(executed when an exception exists) -> onResult</p>
 *
 * @author zhouss
 * @since 2022-07-11
 */
public abstract class AbstractChainHandler implements RequestHandler, Comparable<AbstractChainHandler> {
    protected static final XdsFlowControlConfig XDS_FLOW_CONTROL_CONFIG =
            PluginConfigManager.getPluginConfig(XdsFlowControlConfig.class);

    private AbstractChainHandler next;

    @Override
    public void onBefore(RequestContext context, FlowControlScenario flowControlScenario) {
        AbstractChainHandler cur = getNextHandler(context, flowControlScenario);
        if (cur != null) {
            cur.onBefore(context, flowControlScenario);
        }
    }

    @Override
    public void onThrow(RequestContext context, FlowControlScenario flowControlScenario, Throwable throwable) {
        AbstractChainHandler cur = getNextHandler(context, flowControlScenario);
        if (cur != null) {
            cur.onThrow(context, flowControlScenario, throwable);
        }
    }

    @Override
    public void onResult(RequestContext context, FlowControlScenario flowControlScenario, Object result) {
        AbstractChainHandler cur = getNextHandler(context, flowControlScenario);
        if (cur != null) {
            cur.onResult(context, flowControlScenario, result);
        }
    }

    private AbstractChainHandler getNextHandler(RequestContext context, FlowControlScenario flowControlScenario) {
        AbstractChainHandler tmp = next;
        while (tmp != null) {
            if (!isNeedSkip(tmp, context, flowControlScenario)) {
                break;
            }
            tmp = tmp.getNext();
        }
        return tmp;
    }

    private boolean isNeedSkip(AbstractChainHandler tmp, RequestContext context, FlowControlScenario scenario) {
        final RequestType direct = tmp.direct();
        if (direct != RequestType.BOTH && context.getRequestEntity() != null
                && context.getRequestEntity().getRequestType() != direct) {
            return true;
        }
        final String skipKey = skipCacheKey(tmp);
        Boolean isSkip = context.get(skipKey, Boolean.class);
        if (isSkip == null) {
            isSkip = tmp.isSkip(context, scenario);
            context.save(skipKey, isSkip);
        }
        return isSkip;
    }

    private String skipCacheKey(AbstractChainHandler tmp) {
        String className = tmp.getClass().getName();
        RequestType direct = tmp.direct();

        // The length of the String Builder is initialized for performance
        return direct + "_" + className + "_skip_flag";
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
     * @param context requestContext
     * @param flowControlScenario matched scenario information
     * @return skip or not
     */
    protected boolean isSkip(RequestContext context, FlowControlScenario flowControlScenario) {
        return XDS_FLOW_CONTROL_CONFIG.isEnable();
    }

    @Override
    public int compareTo(AbstractChainHandler handler) {
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

    public void setNext(AbstractChainHandler processor) {
        this.next = processor;
    }

    public AbstractChainHandler getNext() {
        return next;
    }
}
