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

package com.huaweicloud.agentcore.test.application;

import com.huaweicloud.agentcore.test.application.controller.TestController;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * 测试应用启动类
 *
 * @author tangle
 * @since 2023-09-08
 */
public class AgentCoreTestApplication {
    private static final int SERVER_PORT = 8915;

    private static final int REQUEST_SUCCESS_CODE = 200;

    private static final int REQUEST_FAILED_CODE = 404;

    private static final String REQUEST_URL_NOT_FOUND = "No such request path.";

    private static final String REQUEST_PATH_PING = "/ping";

    private static final String REQUEST_PATH_DYNAMIC_CONFIG = "/testDynamicConfig";

    /**
     * 启动main方法
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);

        // 添加URL路由
        server.createContext(REQUEST_PATH_PING, new ControllerHandler());
        server.createContext(REQUEST_PATH_DYNAMIC_CONFIG, new ControllerHandler());

        // 启动服务器
        server.start();
    }

    /**
     * 封装的http服务端
     *
     * @author tangle
     * @since 2023-09-08
     */
    static class ControllerHandler implements HttpHandler {
        private final TestController testController;

        ControllerHandler() {
            this.testController = new TestController();
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 获取输出流
            OutputStream os = exchange.getResponseBody();
            String response = setResponse(exchange);
            int responseCode = response.equals(REQUEST_URL_NOT_FOUND) ? REQUEST_FAILED_CODE : REQUEST_SUCCESS_CODE;

            // 构建响应消息
            exchange.sendResponseHeaders(responseCode, response.length());

            // 发送响应消息
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }

        /**
         * 根据请求url分配执行方法
         *
         * @param exchange
         * @return
         */
        private String setResponse(HttpExchange exchange) {
            switch (exchange.getRequestURI().getPath()) {
                case REQUEST_PATH_PING:
                    return testController.ping();
                case REQUEST_PATH_DYNAMIC_CONFIG:
                    return testController.testDynamicConfig();
                default:
                    return REQUEST_URL_NOT_FOUND;
            }
        }
    }
}
