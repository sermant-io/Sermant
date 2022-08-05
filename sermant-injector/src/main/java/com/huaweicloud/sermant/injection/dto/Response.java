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

package com.huaweicloud.sermant.injection.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * 响应数据
 *
 * @author provenceee
 * @since 2022-07-29
 */
public class Response {
    private static final String JSON_PATCH = "JSONPatch";

    private final String uid;

    private final boolean allowed;

    @JsonInclude(Include.NON_NULL)
    private final String patchType;

    @JsonInclude(Include.NON_NULL)
    private final String patch;

    /**
     * 允许请求的响应
     *
     * @param uid   uid
     * @param patch base64 编码的 JSON Patch 操作数组
     */
    public Response(String uid, String patch) {
        this.uid = uid;
        this.allowed = true;
        this.patchType = patch == null ? null : JSON_PATCH;
        this.patch = patch;
    }

    public String getUid() {
        return uid;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public String getPatchType() {
        return patchType;
    }

    public String getPatch() {
        return patch;
    }
}