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

package io.sermant.injection.dto;

/**
 * Webhook Response
 *
 * @author provenceee
 * @since 2022-07-29
 */
public class WebhookResponse {
    private final String apiVersion;

    private final String kind;

    private final Response response;

    /**
     * Constructor
     *
     * @param apiVersion api version
     * @param kind kind
     * @param response response
     */
    public WebhookResponse(String apiVersion, String kind, Response response) {
        this.apiVersion = apiVersion;
        this.kind = kind;
        this.response = response;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getKind() {
        return kind;
    }

    public Response getResponse() {
        return response;
    }
}
