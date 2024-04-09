/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.registry.entity;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * ScServer Test
 *
 * @author chengyouling
 * @since 2023-01-10
 */
public class ScServerTest {
    private final String serviceName = "test";

    @Test
    public void testScheme() {
        ScServer scServer = new ScServer(buildInstance(8001, true), serviceName);
        Assert.assertEquals(scServer.getScheme(), "https");
        ScServer scServerHttp = new ScServer(buildInstance(8001, false), serviceName);
        Assert.assertEquals(scServerHttp.getScheme(), "http");
    }

    /**
     * Build an instance
     *
     * @param port Port
     * @return Instance
     */
    public MicroServiceInstance buildInstance(int port, boolean secure) {
        return new MicroServiceInstance() {
            @Override
            public String getServiceName() {
                return serviceName;
            }

            @Override
            public String getHost() {
                return "localhost";
            }

            @Override
            public String getIp() {
                return "127.0.0.1";
            }

            @Override
            public int getPort() {
                return port;
            }

            @Override
            public String getServiceId() {
                return serviceName;
            }

            @Override
            public String getInstanceId() {
                return null;
            }

            @Override
            public Map<String, String> getMetadata() {
                return new HashMap<>();
            }

            @Override
            public boolean isSecure() {
                return secure;
            }
        };
    }
}
