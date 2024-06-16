/*
 * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.core.service.httpserver.config;

import io.sermant.core.config.common.BaseConfig;
import io.sermant.core.config.common.ConfigTypeKey;

/**
 * HTTP Server Configuration
 *
 * @author zwmagic
 * @since 2024-02-01
 */
@ConfigTypeKey("httpserver")
public class HttpServerConfig implements BaseConfig {
    private static final int DEFAULT_PORT = 47128;

    /**
     * HTTP Server type, extensible to other HTTP servers
     * <p>
     *     simple: A lightweight HTTP server built into the JDK
     * </p>
     */
    private String type = HttpServerTypeEnum.SIMPLE.getType();

    /**
     * Default port number
     */
    private int port = DEFAULT_PORT;

    /**
     * Core thread pool for HTTP Server
     */
    private Integer serverCorePoolSize;

    /**
     * Maximum thread pool for HTTP Server
     */
    private Integer serverMaxPoolSize;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Integer getServerCorePoolSize() {
        return serverCorePoolSize;
    }

    public void setServerCorePoolSize(Integer serverCorePoolSize) {
        this.serverCorePoolSize = serverCorePoolSize;
    }

    public Integer getServerMaxPoolSize() {
        return serverMaxPoolSize;
    }

    public void setServerMaxPoolSize(Integer serverMaxPoolSize) {
        this.serverMaxPoolSize = serverMaxPoolSize;
    }
}
