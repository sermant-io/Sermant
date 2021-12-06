/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.javamesh.core.lubanops.integration.access.trace;

import java.util.Map;

import com.huawei.javamesh.core.lubanops.integration.access.inbound.EventDataBody;
import com.huawei.javamesh.core.lubanops.integration.access.inbound.EventDataHeader;

/**
 * @author
 * @since 2020/9/23
 **/
public class SpanInfoBuilder {

    public static SpanInfo build(EventDataHeader header, EventDataBody body) {
        SpanInfo spanInfo = new SpanInfo();
        spanInfo.setInstanceId(header.getInstanceId());
        spanInfo.setAppId(header.getAppId());
        spanInfo.setBizId(header.getBizId());
        spanInfo.setEnvId(header.getEnvId());
        spanInfo.setDomainId(header.getDomainId());

        spanInfo.setStartTime(body.getStartTime());
        spanInfo.setTimeUsed(body.getTimeUsed());
        Map<String, String> tags = body.getTags();
        spanInfo.setTags(tags);

        // 对于httpMethod和bizCode，在页面中是需要单独展示的字段
        if (tags != null && tags.get("httpMethod") != null) {
            spanInfo.setMethod(tags.get("httpMethod"));
        }
        if (tags != null && tags.get("bizCode") != null) {
            spanInfo.setBizCode(tags.get("bizCode"));
        }
        spanInfo.setAsync(body.isAsync());
        spanInfo.setClassName(body.getClassName());
        spanInfo.setStatusCode(body.getCode());
        spanInfo.setGlobalTraceId(body.getGlobalTraceId());
        spanInfo.setGlobalPath(body.getGlobalPath());
        spanInfo.setTraceId(body.getTraceId());
        spanInfo.setSpanId(body.getSpanId());
        spanInfo.setHasError(body.getHasError());
        spanInfo.setErrorReasons(body.getErrorReasons());
        spanInfo.setResource(body.getSource());
        spanInfo.setRealSource(body.getRealSource());

        return spanInfo;
    }
}
