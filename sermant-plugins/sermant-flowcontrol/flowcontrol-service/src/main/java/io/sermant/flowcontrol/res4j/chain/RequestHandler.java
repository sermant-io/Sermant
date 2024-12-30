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

import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.res4j.chain.context.RequestContext;

/**
 * request handler definition
 *
 * @author zhouss
 * @since 2022-07-05
 */
public interface RequestHandler {
    /**
     * request processing
     *
     * @param context request context
     * @param flowControlScenario matched business information
     */
    void onBefore(RequestContext context, FlowControlScenario flowControlScenario);

    /**
     * response processing
     *
     * @param context request context
     * @param flowControlScenario matched business information
     * @param result response result
     */
    void onResult(RequestContext context, FlowControlScenario flowControlScenario, Object result);

    /**
     * response processing
     *
     * @param context request context
     * @param flowControlScenario matched business information
     * @param throwable throwable
     */
    void onThrow(RequestContext context, FlowControlScenario flowControlScenario, Throwable throwable);

    /**
     * priority
     *
     * @return priority the smaller the value the higher the priority
     */
    int getOrder();
}
