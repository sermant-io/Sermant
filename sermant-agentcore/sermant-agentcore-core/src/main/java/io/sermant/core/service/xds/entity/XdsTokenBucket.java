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

/**
 * The token bucket configuration to use for rate limiting requests
 *
 * @author zhp
 * @since 2024-11-21
 */
public class XdsTokenBucket {
    /**
     * The maximum tokens that the bucket can hold
     */
    private int maxTokens;

    /**
     * The number of tokens added to the bucket during each fill interval
     */
    private int tokensPerFill;

    /**
     * The fill interval that tokens are added to the bucket
     */
    private long fillInterval;

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public int getTokensPerFill() {
        return tokensPerFill;
    }

    public void setTokensPerFill(int tokensPerFill) {
        this.tokensPerFill = tokensPerFill;
    }

    public long getFillInterval() {
        return fillInterval;
    }

    public void setFillInterval(long fillInterval) {
        this.fillInterval = fillInterval;
    }
}
