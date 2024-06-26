/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sermant.core.command;

import java.util.HashMap;
import java.util.Map;

/**
 * Agent args manager for dynamic install and uninstall
 *
 * @author lilai
 * @since 2024-06-20
 */
public class DynamicAgentArgsManager {
    /**
     * AGENT_ARGS will be updated every time execute dynamic command
     */
    private static final Map<String, String> AGENT_ARGS = new HashMap<>();

    private DynamicAgentArgsManager() {
    }

    /**
     * refresh AGENT_ARGS
     *
     * @param newAgentArgs new coming agent args
     */
    public static void refreshAgentArgs(Map<String, String> newAgentArgs) {
        AGENT_ARGS.putAll(newAgentArgs);
    }

    /**
     * get AGENT_ARGS
     *
     * @param key key
     * @return value
     */
    public static String getAgentArg(String key) {
        return AGENT_ARGS.get(key);
    }
}
