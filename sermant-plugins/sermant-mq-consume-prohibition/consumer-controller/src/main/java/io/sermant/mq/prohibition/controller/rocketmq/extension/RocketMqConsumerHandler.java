/*
 *  Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.mq.prohibition.controller.rocketmq.extension;

import io.sermant.core.plugin.agent.entity.ExecuteContext;

/**
 * Rocketmq Consumer handler interface for external implementations to perform extended operations
 * at rocketmq consumer intercept points
 *
 * @author daizhenyu
 * @since 2023-12-13
 **/
public interface RocketMqConsumerHandler {
    /**
     * Intercept point pre-processing
     *
     * @param context Contextual information
     */
    void doBefore(ExecuteContext context);

    /**
     * Intercept points post-processed
     *
     * @param context Contextual information
     */
    void doAfter(ExecuteContext context);

    /**
     * Exception handling at intercept points
     *
     * @param context Contextual information
     */
    void doOnThrow(ExecuteContext context);
}
