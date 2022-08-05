/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.inject;

import com.huawei.flowcontrol.common.entity.FlowControlResponse;
import com.huawei.flowcontrol.common.entity.FlowControlResult;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 默认响应, 在接管RestTemplate请求，触发治理策略后的默认响应
 *
 * @author zhouss
 * @since 2022-07-21
 */
public class DefaultClientHttpResponse implements ClientHttpResponse {
    private final FlowControlResult flowControlResult;

    private final String contentType;

    private final String msg;

    private InputStream responseStream;

    /**
     * 构造器
     *
     * @param flowControlResult 流控修正结果
     */
    public DefaultClientHttpResponse(FlowControlResult flowControlResult) {
        this.flowControlResult = flowControlResult;
        final FlowControlResponse response = flowControlResult.getResponse();
        if (response.isReplaceResult()) {
            this.contentType = "application/json;charset=utf-8";
        } else {
            this.contentType = "application/text;charset=utf-8";
        }
        this.msg = flowControlResult.buildResponseMsg();
    }

    @Override
    public HttpStatus getStatusCode() {
        return HttpStatus.valueOf(flowControlResult.getResponse().getCode());
    }

    @Override
    public int getRawStatusCode() {
        return flowControlResult.getResponse().getCode();
    }

    @Override
    public String getStatusText() {
        return this.msg;
    }

    @Override
    public void close() {
        if (responseStream != null) {
            try {
                responseStream.close();
            } catch (IOException ignored) {
                // ignored
            }
        }
    }

    @Override
    public InputStream getBody() {
        responseStream = new ByteArrayInputStream(msg.getBytes(StandardCharsets.UTF_8));
        return responseStream;
    }

    @Override
    public HttpHeaders getHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, contentType);
        return httpHeaders;
    }
}
