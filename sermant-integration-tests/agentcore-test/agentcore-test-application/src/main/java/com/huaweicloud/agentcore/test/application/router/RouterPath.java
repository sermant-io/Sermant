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

package com.huaweicloud.agentcore.test.application.router;

/**
 * http请求路由地址
 *
 * @author tangle
 * @since 2023-09-26
 */
public class RouterPath {
    /**
     * 验证服务启动成功的地址
     */
    public static final String REQUEST_PATH_PING = "/ping";

    /**
     * 测试动态配置请求地址
     */
    public static final String REQUEST_PATH_DYNAMIC_CONFIG = "/testDynamicConfig";

    /**
     * 测试动态安装插件请求地址
     */
    public static final String REQUEST_PATH_INSTALL_PLUGIN = "/testInstallPlugin";

    /**
     * 测试动态卸载插件请求地址
     */
    public static final String REQUEST_PATH_UNINSTALL_PLUGIN = "/testUninstallPlugin";

    /**
     * 测试动态卸载Agent请求地址
     */
    public static final String REQUEST_PATH_UNINSTALL_AGENT = "/testUninstallAgent";

    /**
     * 测试动态重装Agent请求地址
     */
    public static final String REQUEST_PATH_REINSTALL_AGENT = "/testReInstallAgent";

    private RouterPath() {
    }
}
