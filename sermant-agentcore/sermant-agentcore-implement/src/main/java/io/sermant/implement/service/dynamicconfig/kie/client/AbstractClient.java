/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.implement.service.dynamicconfig.kie.client;

import io.sermant.implement.service.dynamicconfig.kie.client.http.DefaultHttpClient;
import io.sermant.implement.service.dynamicconfig.kie.client.http.HttpClient;

/**
 * Abstract client
 *
 * @author zhouss
 * @since 2021-11-17
 */
public abstract class AbstractClient implements Client {
    /**
     * Default timeout
     */
    private static final int DEFAULT_TIMEOUT_MS = 60000;

    /**
     * Client url manager
     */
    protected final ClientUrlManager clientUrlManager;

    /**
     * http client
     */
    protected HttpClient httpClient;

    /**
     * Constructor.
     *
     * @param clientUrlManager clientUrlManager
     */
    protected AbstractClient(ClientUrlManager clientUrlManager) {
        this.clientUrlManager = clientUrlManager;
        initDefaultClient(DEFAULT_TIMEOUT_MS);
    }

    /**
     * Constructor.
     *
     * @param clientUrlManager clientUrlManager
     * @param httpClient httpClient
     * @param timeout timeout
     */
    protected AbstractClient(ClientUrlManager clientUrlManager, HttpClient httpClient, int timeout) {
        this.clientUrlManager = clientUrlManager;
        if (httpClient != null) {
            this.httpClient = httpClient;
        } else {
            initDefaultClient(timeout);
        }
    }

    private void initDefaultClient(int timeout) {
        this.httpClient = new DefaultHttpClient(timeout);
    }
}
