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

package com.example.sermant.agentcore.test.application.tests.dynamic;

import com.example.sermant.agentcore.test.application.results.DynamicResults;

/**
 * Test class for dynamic installation and uninstallation
 *
 * @author tangle
 * @since 2023-09-08
 */
public class DynamicTest {
    /**
     * Index of the boolean array, corresponding to the interceptor variables of the three plugins
     */
    private static final int FIRST_PLUGIN_ENHANCE_INDEX = 0;
    private static final int SECOND_PLUGIN_ENHANCE_INDEX = 1;
    private static final int THIRD_PLUGIN_ENHANCE_INDEX = 2;
    private static final int FIRST_PLUGIN_V2_ENHANCE_INDEX = 3;
    /**
     * Result of the boolean array for testing plugin reflection result: listener success
     */
    private static boolean serviceCloseSuccess;

    public static void setServiceCloseSuccess(boolean flag) {
        serviceCloseSuccess = flag;
    }

    /**
     * Test dynamic installation plugin
     */
    public void testInstallPlugin() {
        boolean[] result = repeatEnhance(false, false, false);
        if (result[FIRST_PLUGIN_ENHANCE_INDEX] && result[SECOND_PLUGIN_ENHANCE_INDEX]) {
            DynamicResults.DYNAMIC_INSTALL_PLUGIN_REPEAT_ENHANCE.setResult(true);
        }
    }

    /**
     * Test dynamic upgrade plugin
     */
    public void testUpdatePlugin() {
        boolean[] result = repeatEnhance(false, false, false);
        if (result[SECOND_PLUGIN_ENHANCE_INDEX] && result[THIRD_PLUGIN_ENHANCE_INDEX]) {
            DynamicResults.DYNAMIC_UPDATE_PLUGIN.setResult(true);
        }
    }

    /**
     * Test dynamic uninstall plugin
     */
    public void testUninstallPlugin() {
        boolean[] result = repeatEnhance(false, false, false);
        if (!result[FIRST_PLUGIN_ENHANCE_INDEX]) {
            DynamicResults.DYNAMIC_UNINSTALL_PLUGIN_INTERCEPTOR_FAILURE.setResult(true);
        }
        if (result[SECOND_PLUGIN_ENHANCE_INDEX]) {
            DynamicResults.DYNAMIC_UNINSTALL_REPEAT_ENHANCE.setResult(true);
        }
    }

    /**
     * Test dynamic uninstall Agent
     */
    public void testUninstallAgent() {
        boolean[] result = repeatEnhance(false, false, false);
        if (!result[FIRST_PLUGIN_ENHANCE_INDEX] && !result[SECOND_PLUGIN_ENHANCE_INDEX]) {
            DynamicResults.DYNAMIC_UNINSTALL_AGENT_PLUGIN_FAILURE.setResult(true);
        }
    }

    /**
     * Test dynamic reinstall Agent
     */
    public void testReInstallAgent() {
        boolean[] result = repeatEnhance(false, false, false);
        if (result[FIRST_PLUGIN_ENHANCE_INDEX] && result[SECOND_PLUGIN_ENHANCE_INDEX]) {
            DynamicResults.DYNAMIC_REINSTALL_AGENT_PLUGIN_SUCCESS.setResult(true);
        }
    }

    /**
     * Test premain startup
     */
    public void testPremainStartup() {
        boolean[] result = repeatEnhance(false, false, false);
        if (result[FIRST_PLUGIN_ENHANCE_INDEX]) {
            DynamicResults.PREMAIN_STATIC_PLUGIN_INTERCEPTOR_SUCCESS.setResult(true);
        }
        if (!result[SECOND_PLUGIN_ENHANCE_INDEX]) {
            DynamicResults.PREMAIN_DYNAMIC_PLUGIN_INTERCEPTOR_FAILURE.setResult(true);
        }
    }

    /**
     * Test agentmain startup
     */
    public void testAgentmainStartup() {
        boolean[] result = repeatEnhance(false, false, false);
        if (!result[FIRST_PLUGIN_ENHANCE_INDEX]) {
            DynamicResults.AGENTMAIN_STATIC_PLUGIN_INTERCEPTOR_FAILURE.setResult(true);
        }
        if (result[SECOND_PLUGIN_ENHANCE_INDEX]) {
            DynamicResults.AGENTMAIN_ACTIVE_PLUGIN_INTERCEPTOR_SUCCESS.setResult(true);
        }
        if (!result[THIRD_PLUGIN_ENHANCE_INDEX]) {
            DynamicResults.AGENTMAIN_PASSIVE_PLUGIN_INTERCEPTOR_FAILURE.setResult(true);
        }
    }

    /**
     * Test plugin enhancement intercept method
     *
     * @param firstEnhanceFlag first-plugin enhance flag
     * @param secondEnhanceFlag second-plugin enhance flag
     * @param thirdEnhanceFlag second-plugin enhance flag
     * @return Enhance result array
     */
    private boolean[] repeatEnhance(boolean firstEnhanceFlag, boolean secondEnhanceFlag, boolean thirdEnhanceFlag) {
        return new boolean[]{firstEnhanceFlag, secondEnhanceFlag, thirdEnhanceFlag};
    }
}
