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

package io.sermant.flowcontrol.service.rest4j;

import io.sermant.core.plugin.service.PluginService;
import io.sermant.flowcontrol.common.entity.FlowControlResult;
import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.common.entity.RequestEntity;

/**
 * XDS flow control service, used to execute flow control processor chains for HTTP clients, Currently,
 * the handlers are rate limited handler and error injection handler
 *
 * @author zhp
 * @since 2024-12-28
 */
public interface XdsHttpFlowControlService extends PluginService {
    /**
     * Used to perform pre operation of the flow control processor chain before the request
     *
     * @param requestEntity request information
     * @param fixedResult fixed result
     */
    void onBefore(RequestEntity requestEntity, FlowControlResult fixedResult);

    /**
     * Used to perform post operations on the flow control processor chain after a request
     *
     * @param requestEntity request information
     * @param result response result
     * @param flowControlScenario Scenario information for flow control
     */
    void onAfter(RequestEntity requestEntity, Object result, FlowControlScenario flowControlScenario);

    /**
     * Used to handle exceptions in the flow control processor chain during request exceptions
     *
     * @param requestEntity request information
     * @param throwable exception message
     * @param flowControlScenario Scenario information for flow control
     */
    void onThrow(RequestEntity requestEntity, Throwable throwable, FlowControlScenario flowControlScenario);
}
