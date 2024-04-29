/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.sermant.visibility.common;

/**
 * Constant classes
 *
 * @author zhp
 * @since 2022-12-01
 */
public class Constants {
    /**
     * The name of the app
     */
    public static final String APPLICATION_FIELD_NAME = "application";

    /**
     * Method Name
     */
    public static final String METHOD_FIELD_NAME = "methods";

    /**
     * The name of the interface
     */
    public static final String INTERFACE_FIELD_NAME = "interface";

    /**
     * Server IP
     */
    public static final String IP_FIELD_NAME = "bind.ip";

    /**
     * Server port
     */
    public static final String PORT_FIELD_NAME = "bind.port";

    /**
     * The address of the request
     */
    public static final String PATH_FIELD_NAME = "path";

    private Constants() {
    }
}
