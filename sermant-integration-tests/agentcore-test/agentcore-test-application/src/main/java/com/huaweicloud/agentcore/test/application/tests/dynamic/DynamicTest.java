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

package com.huaweicloud.agentcore.test.application.tests.dynamic;

import com.huaweicloud.agentcore.test.application.results.DynamicResults;

/**
 * 动态安装卸载测试类
 *
 * @author tangle
 * @since 2023-09-08
 */
public class DynamicTest {
    /**
     * 测试的boolean数组下标,对应着三个插件的拦截修改变量
     */
    private static final int FIRST_PLUGIN_ENHANCE_INDEX = 0;
    private static final int SECOND_PLUGIN_ENHANCE_INDEX = 1;
    private static final int THIRD_PLUGIN_ENHANCE_INDEX = 2;
    /**
     * 用于测试插件反射修改的回执结果：监听成功
     */
    private static boolean serviceCloseSuccess;

    public static void setServiceCloseSuccess(boolean flag) {
        serviceCloseSuccess = flag;
    }

    /**
     * 测试动态安装插件
     */
    public void testInstallPlugin() {
        boolean[] result = repeatEnhance(false, false, false);
        if (result[FIRST_PLUGIN_ENHANCE_INDEX] && result[SECOND_PLUGIN_ENHANCE_INDEX]) {
            DynamicResults.DYNAMIC_INSTALL_PLUGIN_REPEAT_ENHANCE.setResult(true);
        }
    }

    /**
     * 测试动态卸载插件
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
     * 测试动态卸载Agent
     */
    public void testUninstallAgent() {
        boolean[] result = repeatEnhance(false, false, false);
        if (!result[FIRST_PLUGIN_ENHANCE_INDEX] && !result[SECOND_PLUGIN_ENHANCE_INDEX]) {
            DynamicResults.DYNAMIC_UNINSTALL_AGENT_PLUGIN_FAILURE.setResult(true);
        }
    }

    /**
     * 测试动态重装Agent
     */
    public void testReInstallAgent() {
        boolean[] result = repeatEnhance(false, false, false);
        if (result[FIRST_PLUGIN_ENHANCE_INDEX] && result[SECOND_PLUGIN_ENHANCE_INDEX]) {
            DynamicResults.DYNAMIC_REINSTALL_AGENT_PLUGIN_SUCCESS.setResult(true);
        }
    }

    /**
     * 测试premain启动
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
     * 测试agentmain启动
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
     * 测试插件的增强拦截方法
     *
     * @param firstEnhanceFlag first-plugin的增强flag
     * @param secondEnhanceFlag second-plugin的增强flag
     * @param thirdEnhanceFlag second-plugin的增强flag
     * @return 增强结果数组
     */
    private boolean[] repeatEnhance(boolean firstEnhanceFlag, boolean secondEnhanceFlag, boolean thirdEnhanceFlag) {
        return new boolean[]{firstEnhanceFlag, secondEnhanceFlag, thirdEnhanceFlag};
    }
}
