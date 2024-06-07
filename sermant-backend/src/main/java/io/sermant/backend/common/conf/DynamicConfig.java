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

package io.sermant.backend.common.conf;

import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Configuration class for dynamic configuration function
 *
 * @author zhp
 * @since 2024-05-21
 */
@Configuration
@Getter
@Setter
@Component
public class DynamicConfig {
    /**
     * Connection address of configuration center
     */
    @Value("${dynamic.config.serverAddress}")
    private String serverAddress;

    /**
     * Type of Configuration Center
     */
    @Value("${dynamic.config.dynamicConfigType}")
    private String dynamicConfigType;

    /**
     * Connection timeout
     */
    @Value("${dynamic.config.connectTimeout}")
    private long connectTimeout;

    /**
     * Session timeout
     */
    @Value("${dynamic.config.timeout}")
    private int timeout;

    /**
     * Authorized switch
     */
    @Value("${dynamic.config.enableAuth}")
    private boolean enableAuth;

    /**
     * userName, used for configuration center connection authorization
     */
    @Value("${dynamic.config.userName}")
    private String userName;

    /**
     * password, used for configuration center connection authorization
     */
    @Value("${dynamic.config.password}")
    private String password;

    /**
     * Encryption and decryption keys for passwords
     */
    @Value("${dynamic.config.secretKey}")
    private String secretKey;

    /**
     * Namespaces, only used by the nacos configuration center
     */
    @Value("${dynamic.config.namespace}")
    private String namespace = "default";

    /**
     * Switch for configuration management
     */
    @Value("${dynamic.config.dynamicConfigEnable}")
    private boolean dynamicConfigEnable;
}
