/*
 * Copyright (C) 2024-2025 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.xds.service.traffic.management.handler.exception;

import io.sermant.core.service.xds.entity.XdsHeader;
import io.sermant.core.service.xds.entity.XdsHeaderOption;
import io.sermant.core.utils.StringUtils;
import io.sermant.xds.common.entity.FlowControlResponse;
import io.sermant.xds.common.entity.FlowControlResult;
import io.sermant.xds.service.traffic.management.exception.RateLimitException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * flow control exception handler
 *
 * @author zhouss
 * @since 2024-12-05
 */
public class RateLimitingExceptionHandler extends AbstractExceptionHandler<RateLimitException> {
    @Override
    protected FlowControlResponse getFlowControlResponse(RateLimitException ex, FlowControlResult flowControlResult) {
        Map<String, List<String>> headers = new HashMap<>();
        for (XdsHeaderOption xdsHeaderOption : ex.getXdsHeaderOptions()) {
            XdsHeader header = xdsHeaderOption.getHeader();
            if (header == null || StringUtils.isEmpty(header.getKey())) {
                continue;
            }
            if (xdsHeaderOption.isEnabledAppend() && headers.containsKey(header.getKey())) {
                List<String> value = headers.get(header.getKey());
                value.set(0, value.get(0) + "," + header.getValue());
                continue;
            }
            headers.put(header.getKey(), Collections.singletonList(header.getValue()));
        }
        return new FlowControlResponse(ex.getMsg(), ex.getCode(), headers);
    }

    @Override
    public Class<RateLimitException> targetException() {
        return RateLimitException.class;
    }
}
