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

import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.common.entity.RequestEntity;

/**
 * Xds request handler definition
 *
 * @author zhp
 * @since 2024-12-31
 */
public interface XdsRequestHandler {
    /**
     * request processing
     *
     * @param requestEntity request-information
     * @param flowControlScenario matched business information
     */
    void onBefore(RequestEntity requestEntity, FlowControlScenario flowControlScenario);

    /**
     * response processing
     *
     * @param requestEntity request-information
     * @param flowControlScenario matched business information
     * @param result response result
     */
    void onAfter(RequestEntity requestEntity, FlowControlScenario flowControlScenario, Object result);

    /**
     * response processing
     *
     * @param requestEntity request-information
     * @param flowControlScenario matched business information
     * @param throwable throwable
     */
    void onThrow(RequestEntity requestEntity, FlowControlScenario flowControlScenario, Throwable throwable);

    /**
     * priority
     *
     * @return priority the smaller the value the higher the priority
     */
    int getOrder();
}
