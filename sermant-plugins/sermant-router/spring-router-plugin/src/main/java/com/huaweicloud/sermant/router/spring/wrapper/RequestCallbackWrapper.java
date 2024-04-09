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

package com.huaweicloud.sermant.router.spring.wrapper;

import org.springframework.http.client.ClientHttpRequest;
import org.springframework.web.client.RequestCallback;

import java.io.IOException;
import java.util.Map;

/**
 * A wrapper class for {@link RequestCallback} that passes the request header <br>information
 *
 * @author yuzl Yu Zhenlong
 * @since 2022-10-27
 */
public class RequestCallbackWrapper implements RequestCallback {
    private final RequestCallback callback;

    private final Map<String, String> header;

    /**
     * Constructor
     *
     * @param callback Objects that really need to be packaged
     * @param header Request header information
     */
    public RequestCallbackWrapper(RequestCallback callback, Map<String, String> header) {
        this.callback = callback;
        this.header = header;
    }

    @Override
    public void doWithRequest(ClientHttpRequest request) throws IOException {
        this.callback.doWithRequest(request);
    }

    public Map<String, String> getHeader() {
        return header;
    }
}
