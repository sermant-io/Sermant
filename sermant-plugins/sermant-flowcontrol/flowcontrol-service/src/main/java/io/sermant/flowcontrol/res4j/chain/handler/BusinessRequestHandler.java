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

import io.sermant.core.utils.CollectionUtils;
import io.sermant.flowcontrol.common.core.match.MatchManager;
import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.res4j.chain.AbstractChainHandler;
import io.sermant.flowcontrol.res4j.chain.HandlerConstants;
import io.sermant.flowcontrol.res4j.chain.context.ChainContext;
import io.sermant.flowcontrol.res4j.chain.context.RequestContext;

import java.util.Set;

/**
 * The service matches the handler with the highest priority（in addition to surveillance）
 *
 * @author zhouss
 * @since 2022-07-05
 */
public class BusinessRequestHandler extends AbstractChainHandler {
    private static final String MATCHED_SCENARIO_INFO = "__MATCHED_SCENARIO_INFO__";

    @Override
    public void onBefore(RequestContext context, FlowControlScenario flowControlScenario) {
        final Set<String> matchedBusinessNames = MatchManager.INSTANCE.matchWithCache(context.getRequestEntity());
        if (CollectionUtils.isEmpty(matchedBusinessNames)) {
            return;
        }
        FlowControlScenario scenarioInfo = new FlowControlScenario();
        scenarioInfo.setMatchedScenarioNames(matchedBusinessNames);
        context.save(MATCHED_SCENARIO_INFO, scenarioInfo);
        super.onBefore(context, scenarioInfo);
    }

    @Override
    public void onThrow(RequestContext context, FlowControlScenario flowControlScenario, Throwable throwable) {
        final FlowControlScenario scenarioInfo = getMatchedScenarioInfo(context);
        if (scenarioInfo == null || CollectionUtils.isEmpty(scenarioInfo.getMatchedScenarioNames())) {
            return;
        }
        super.onThrow(context, scenarioInfo, throwable);
    }

    @Override
    public void onResult(RequestContext context, FlowControlScenario flowControlScenario, Object result) {
        final FlowControlScenario scenarioInfo = getMatchedScenarioInfo(context);
        if (scenarioInfo == null || CollectionUtils.isEmpty(scenarioInfo.getMatchedScenarioNames())) {
            return;
        }
        try {
            super.onResult(context, scenarioInfo, result);
        } finally {
            ChainContext.getThreadLocalContext(context.getSourceName()).remove(MATCHED_SCENARIO_INFO);
        }
    }

    private FlowControlScenario getMatchedScenarioInfo(RequestContext context) {
        return context.get(MATCHED_SCENARIO_INFO, FlowControlScenario.class);
    }

    @Override
    public int getOrder() {
        return HandlerConstants.BUSINESS_ORDER;
    }
}
