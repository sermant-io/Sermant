/*
 *  Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.database.handler;

import io.sermant.core.plugin.agent.entity.ExecuteContext;

/**
 * database write forbidden processing interface
 *
 * @author daizhenyu
 * @since 2024-01-15
 **/
public interface DatabaseHandler {
    /**
     * intercept point preprocessing
     *
     * @param context contextual information
     */
    void doBefore(ExecuteContext context);

    /**
     * intercept point post processing
     *
     * @param context contextual information
     */
    void doAfter(ExecuteContext context);

    /**
     * intercept point exception handling
     *
     * @param context contextual information
     */
    void doOnThrow(ExecuteContext context);
}
