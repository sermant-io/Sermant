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

package io.sermant.flowcontrol.res4j.service;

import io.sermant.flowcontrol.common.entity.FlowControlResult;
import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.common.entity.RequestEntity;
import io.sermant.flowcontrol.res4j.chain.XdsHandlerChainEntry;
import io.sermant.flowcontrol.service.rest4j.XdsHttpFlowControlService;

/**
 * http request interception logic implementation
 *
 * @author zhp
 * @since 2024-12-28
 */
public class XdsHttpFlowControlServiceImpl implements XdsHttpFlowControlService {
    @Override
    public void onBefore(RequestEntity requestEntity, FlowControlResult flowControlResult) {
        XdsHandlerChainEntry.INSTANCE.onBefore(requestEntity, flowControlResult);
    }

    @Override
    public void onAfter(RequestEntity requestEntity, Object result, FlowControlScenario flowControlScenario) {
        XdsHandlerChainEntry.INSTANCE.onAfter(requestEntity, result, flowControlScenario);
    }

    @Override
    public void onThrow(RequestEntity requestEntity, Throwable throwable, FlowControlScenario flowControlScenario) {
        XdsHandlerChainEntry.INSTANCE.onThrow(requestEntity, throwable, flowControlScenario);
    }
}
