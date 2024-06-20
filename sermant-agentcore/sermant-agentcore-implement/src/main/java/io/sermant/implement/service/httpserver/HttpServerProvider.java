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

package io.sermant.implement.service.httpserver;

/**
 * HTTP server provider interface.
 *
 * @author zwmagic
 * @since 2024-02-02
 */
public interface HttpServerProvider {
    /**
     * Get Http server type
     *
     * @return Http server Type
     */
    String getType();

    /**
     * Start HTTP server
     *
     * @throws Exception Exception
     */
    void start() throws Exception;

    /**
     * Stop HTTP server
     *
     * @throws Exception Exception
     */
    void stop() throws Exception;
}
