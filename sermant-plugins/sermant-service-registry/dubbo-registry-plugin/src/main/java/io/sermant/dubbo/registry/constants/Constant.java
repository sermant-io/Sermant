/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.dubbo.registry.constants;

/**
 * Constant
 *
 * @author provenceee
 * @since 2022-01-27
 */
public class Constant {
    /**
     * SC registration protocol name
     */
    public static final String SC_REGISTRY_PROTOCOL = "sc";

    /**
     * NACOS registers the name of the protocol
     */
    public static final String NACOS_REGISTRY_PROTOCOL = "nacos";

    /**
     * SC registry ip
     */
    public static final String SC_REGISTRY_IP = "localhost:30100";

    /**
     * Protocol delimiter
     */
    public static final String PROTOCOL_SEPARATION = "://";

    /**
     * SC initializes the migration rule
     */
    public static final String SC_INIT_MIGRATION_RULE = "scInit";

    /**
     * SC registry address
     */
    public static final String SC_REGISTRY_ADDRESS = SC_REGISTRY_PROTOCOL + PROTOCOL_SEPARATION + SC_REGISTRY_IP;

    /**
     * Method name for setting protocol
     */
    public static final String SET_PROTOCOL_METHOD_NAME = "setProtocol";

    private Constant() {
    }
}
