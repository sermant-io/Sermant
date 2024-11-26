/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.core.service.xds.entity;

import java.util.List;

/**
 * Xds rate-limiting configuration information
 *
 * @author zhp
 * @since 2024-11-18
 */
public class XdsRateLimit {
    /**
     * configuration for token bucket
     */
    private XdsTokenBucket tokenBucket;

    /**
     * Configuration for Response Header Operations
     */
    private List<XdsHeaderOption> responseHeaderOption;

    /**
     * The value of FractionalPercent，If set, the rate limit decisions for the given fraction of requests
     */
    private FractionalPercent percent;

    public XdsTokenBucket getTokenBucket() {
        return tokenBucket;
    }

    public void setTokenBucket(XdsTokenBucket tokenBucket) {
        this.tokenBucket = tokenBucket;
    }

    public List<XdsHeaderOption> getResponseHeaderOption() {
        return responseHeaderOption;
    }

    public void setResponseHeaderOption(List<XdsHeaderOption> responseHeaderOption) {
        this.responseHeaderOption = responseHeaderOption;
    }

    public FractionalPercent getPercent() {
        return percent;
    }

    public void setPercent(FractionalPercent percent) {
        this.percent = percent;
    }
}
