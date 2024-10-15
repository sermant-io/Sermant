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

package com.example.sermant.agentcore.test.application.router;

import com.alibaba.fastjson.JSONObject;
import com.example.sermant.agentcore.test.application.controller.TestController;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * 封装的http服务端
 *
 * @author tangle
 * @since 2023-09-08
 */
public class ControllerHandler implements HttpHandler {
    private static final int REQUEST_SUCCESS_CODE = 200;

    private static final int REQUEST_NOT_FOUND_CODE = 404;

    private static final int REQUEST_FAILED_CODE = 500;

    private static final String REQUEST_URL_NOT_FOUND = "No such request path.";

    private static final String REQUEST_PARAMS_ERROR = "Request params error.";

    private final TestController testController;

    /**
     * 构造函数
     */
    public ControllerHandler() {
        this.testController = new TestController();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Object responseObject = setResponse(exchange);
        String response = "";
        int responseCode = REQUEST_SUCCESS_CODE;
        if (responseObject instanceof String) {
            if (responseObject.equals(REQUEST_URL_NOT_FOUND)) {
                responseCode = REQUEST_NOT_FOUND_CODE;
            }
            if (responseObject.equals(REQUEST_PARAMS_ERROR)) {
                responseCode = REQUEST_FAILED_CODE;
            }
            response = (String) responseObject;
        } else {
            response = JSONObject.toJSONString(responseObject);
        }

        // 构建响应消息
        exchange.sendResponseHeaders(responseCode, response.length());

        // 发送响应消息
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    /**
     * 根据请求url分配执行方法
     *
     * @param exchange
     * @return
     */
    private Object setResponse(HttpExchange exchange) {
        switch (exchange.getRequestURI().getPath()) {
            case RouterPath.REQUEST_PATH_PING:
                return testController.ping();
            case RouterPath.REQUEST_PATH_DYNAMIC_CONFIG:
                return testController.testDynamicConfig();
            case RouterPath.REQUEST_PATH_INSTALL_PLUGIN:
                return testController.testInstallPlugin();
            case RouterPath.REQUEST_PATH_UNINSTALL_PLUGIN:
                return testController.testUninstallPlugin();
            case RouterPath.REQUEST_PATH_UNINSTALL_AGENT:
                return testController.testUninstallAgent();
            case RouterPath.REQUEST_PATH_REINSTALL_AGENT:
                return testController.testReInstallAgent();
            case RouterPath.REQUEST_PATH_PREMAIN_STARTUP:
                return testController.testPremainStartup();
            case RouterPath.REQUEST_PATH_AGENTMAIN_STARTUP:
                return testController.testAgentmainStartup();
            case RouterPath.REQUEST_PATH_CORE_AND_PLUGIN_CONFIG_LOAD:
                return testController.testCoreAndPluginConfigLoad();
            case RouterPath.REQUEST_PATH_CLASS_MATCH:
                return testController.testClassMatch();
            case RouterPath.REQUEST_PATH_METHOD_MATCH:
                return testController.testMethodMatch();
            case RouterPath.REQUEST_PATH_ENHANCEMENT:
                return testController.testEnhancement();
            case RouterPath.REQUEST_PATH_RE_TRANSFORM:
                return testController.testReTransform();
            case RouterPath.UPDATE_PLUGIN:
                return testController.testUpdatePlugin();
            default:
                return REQUEST_URL_NOT_FOUND;
        }
    }
}
