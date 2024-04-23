/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.premain.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Agent arguments resolver
 *
 * @author luanwenfei
 * @since 2023-09-23
 */
public class AgentArgsResolver {
    private AgentArgsResolver() {
    }

    /**
     * Parse Agent arguments, split arguments with ",", and KEY and VALUE with "="
     *
     * @param agentArgs Agent arguments string
     * @return Resolve result
     * @throws IllegalArgumentException Agent arguments are unavailable
     */
    public static Map<String, Object> resolveAgentArgs(String agentArgs) {
        final Map<String, Object> argsMap = new HashMap<>();
        if (agentArgs == null) {
            return argsMap;
        }
        for (String arg : agentArgs.trim().split(",")) {
            final int index = arg.indexOf('=');
            if (index > 0) {
                argsMap.put(arg.substring(0, index).trim(), arg.substring(index + 1).trim());
                continue;
            }
            throw new IllegalArgumentException("Agent argument cannot be resolved, argument is: " + arg);
        }
        return argsMap;
    }
}
