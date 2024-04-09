/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol.common.entity;

import com.huawei.flowcontrol.common.entity.RequestEntity.RequestType;

/**
 * fix the result that determines the returned data
 *
 * @author zhouss
 * @since 2022-02-09
 */
public class FlowControlResult {
    /**
     * flow control response
     */
    private FlowControlResponse response;

    /**
     * whether to skip the call
     */
    private boolean isSkip = false;

    /**
     * request direction
     */
    private RequestType requestType;

    /**
     * build response prompt, requestType
     *
     * @return response message
     */
    public String buildResponseMsg() {
        if (response == null) {
            return "";
        }
        if (response.isReplaceResult()) {
            // Replace the scenario and use the serialized result directly
            return response.getSerializeResult();
        } else {
            return response.getMsg();
        }
    }

    public boolean isSkip() {
        return isSkip;
    }

    public void setSkip(boolean isNeedSkip) {
        this.isSkip = isNeedSkip;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public FlowControlResponse getResponse() {
        return response;
    }

    /**
     * Changes the result of the flow control response and is told to skip the method call itself
     *
     * @param response response
     */
    public void setResponse(FlowControlResponse response) {
        this.response = response;
        this.isSkip = true;
    }
}
