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

package io.sermant.core.service.httpserver.api;

/**
 * HTTP Route Handler Adapter Abstract Class
 *
 * @author zwmagic
 * @since 2024-02-03
 */
public abstract class HttpRouteHandlerAdapter implements HttpRouteHandler {
    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        try {
            ResponseResult<?> result = doHandle(request);
            response.writeBodyAsJson(result);
        } catch (Exception e) {
            response.writeBodyAsJson(ResponseResult.ofFailure(e));
        }
    }

    /**
     * Executes the method to process the request.
     *
     * @param <T> Generic type
     * @param request HTTP request object
     * @return Result object after processing the request
     */
    public abstract <T> ResponseResult<T> doHandle(HttpRequest request);
}
