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

package com.huaweicloud.integration.service.impl;

import com.huaweicloud.integration.service.FlowControlService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 流控测试
 *
 * @author zhouss
 * @since 2022-09-15
 */
public abstract class FlowControlServiceImpl implements FlowControlService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlowControlServiceImpl.class);

    private static final String OK = "ok";

    private static final long FAULT_DELAY_MS = 500L;

    private static final long SLEEP_MS = 100L;

    @Override
    public String rateLimiting() {
        return OK;
    }

    @Override
    public String rateLimitingPrefix() {
        return OK;
    }

    @Override
    public String rateLimitingSuffix() {
        return OK;
    }

    @Override
    public String rateLimitingContains() {
        return OK;
    }

    @Override
    public String rateLimitingWithApplication() {
        return rateLimiting();
    }

    @Override
    public String rateLimitingWithHeader(Map<String, Object> attachments) {
        LOGGER.info("accepted attachments {}", attachments);
        return rateLimiting();
    }

    @Override
    public String cirSlowInvoker() {
        try {
            Thread.sleep(SLEEP_MS);
        } catch (InterruptedException ignored) {
            // ignored
        }
        return OK;
    }

    @Override
    public String cirEx() {
        throw new IllegalStateException("cir test");
    }

    @Override
    public String instanceSlowInvoker() {
        return cirSlowInvoker();
    }

    @Override
    public String instanceEx() {
        return cirEx();
    }

    @Override
    public String faultNull() {
        return OK;
    }

    @Override
    public String faultThrowEx() {
        return OK;
    }

    @Override
    public String faultDelay() {
        try {
            Thread.sleep(FAULT_DELAY_MS);
        } catch (InterruptedException ignored) {
            // ignored
        }
        return OK;
    }

    @Override
    public String bulkhead() {
        return cirSlowInvoker();
    }

    @Override
    public void lb() {
    }
}
