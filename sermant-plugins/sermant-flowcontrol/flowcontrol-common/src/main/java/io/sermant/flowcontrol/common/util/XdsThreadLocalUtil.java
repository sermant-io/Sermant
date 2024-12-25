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

package io.sermant.flowcontrol.common.util;

import io.sermant.flowcontrol.common.entity.FlowControlScenario;

/**
 * xds thread local utility class
 *
 * @author zhp
 * @since 2024-11-30
 */
public class XdsThreadLocalUtil {
    private static final ThreadLocal<Boolean> SEND_BYTE_FLAG = new ThreadLocal<>();

    private static final ThreadLocal<FlowControlScenario> FLOW_CONTROL_SCENARIO_THREAD_LOCAL = new ThreadLocal<>();

    private XdsThreadLocalUtil() {
    }

    /**
     * Set byte send flag
     *
     * @param flag byte send flag
     */
    public static void setSendByteFlag(boolean flag) {
        SEND_BYTE_FLAG.set(flag);
    }

    /**
     * get byte send flag
     *
     * @return byte send flag
     */
    public static boolean getSendByteFlag() {
        return SEND_BYTE_FLAG.get() != null && SEND_BYTE_FLAG.get();
    }

    /**
     * remove byte send flag
     */
    public static void removeSendByteFlag() {
        SEND_BYTE_FLAG.remove();
    }

    /**
     * Set scenario information
     *
     * @param flowControlScenario scenario information
     */
    public static void setScenarioInfo(FlowControlScenario flowControlScenario) {
        FLOW_CONTROL_SCENARIO_THREAD_LOCAL.set(flowControlScenario);
    }

    /**
     * get scenario information
     *
     * @return flowControl scenario information
     */
    public static FlowControlScenario getScenarioInfo() {
        return FLOW_CONTROL_SCENARIO_THREAD_LOCAL.get();
    }

    /**
     * remove business information
     */
    public static void removeScenarioInfo() {
        FLOW_CONTROL_SCENARIO_THREAD_LOCAL.remove();
    }
}
