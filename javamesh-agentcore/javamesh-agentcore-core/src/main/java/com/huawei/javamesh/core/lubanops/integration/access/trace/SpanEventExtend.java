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

package com.huawei.javamesh.core.lubanops.integration.access.trace;

import com.huawei.javamesh.core.lubanops.integration.access.inbound.EventDataBody;
import com.huawei.javamesh.core.lubanops.integration.access.inbound.EventDataHeader;

/**
 * 此类集成自SpanEventReport ，主要包含一些需要后期计算的字段，用此类反序列化，对象，可以减少对象转换成本 <br>
 *
 * @author
 * @since 2020年3月4日
 */
public class SpanEventExtend extends SpanEventReport {

    /**
     * 将上报的header和body信息拼接成一个对象
     *
     * @param header
     * @param body
     * @return
     */
    public static SpanEventExtend build(EventDataHeader header, EventDataBody body) {
        SpanEventExtend event = new SpanEventExtend();
        event.setInstanceId(header.getInstanceId());
        event.setAppId(header.getAppId());
        event.setBizId(header.getBizId());
        event.setEnvId(header.getEnvId());
        event.setDomainId(header.getDomainId());
        event.setAttachment(header.getAttachment());
        event.setStartTime(body.getStartTime());
        event.setTimeUsed(body.getTimeUsed());
        event.setTags(body.getTags());
        event.setAsync(body.isAsync());
        event.setClassName(body.getClassName());
        event.setMethod(body.getMethod());
        event.setTraceId(body.getTraceId());
        event.setGlobalTraceId(body.getGlobalTraceId());
        event.setGlobalPath(body.getGlobalPath());
        event.setSpanId(body.getSpanId());
        event.setEventId(body.getEventId());
        event.setHasError(body.getHasError());
        event.setErrorReasons(body.getErrorReasons());
        event.setChildrenEventCount(body.getChildrenEventCount());
        event.setNextSpanId(body.getNextSpanId());
        event.setType(body.getType());
        event.setSource(body.getSource());
        event.setRealSource(body.getRealSource());
        event.setArgument(body.getArgument());
        event.populate();

        return event;

    }

    /**
     * 根据属性计算 <br>
     *
     * @author
     * @since 2020年3月4日
     */
    public void populate() {

    }

}
