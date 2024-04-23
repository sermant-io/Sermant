/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.removal.entity;

/**
 * Request statistics
 *
 * @author zhp
 * @since 2023-02-24
 */
public class RequestCountData {
    /**
     * Number of requests
     */
    private int requestNum;

    /**
     * The number of failed requests
     */
    private int requestFailNum;

    /**
     * Error rate
     */
    private float errorRate;

    public int getRequestNum() {
        return requestNum;
    }

    public void setRequestNum(int requestNum) {
        this.requestNum = requestNum;
    }

    public int getRequestFailNum() {
        return requestFailNum;
    }

    public void setRequestFailNum(int requestFailNum) {
        this.requestFailNum = requestFailNum;
    }

    public float getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(float errorRate) {
        this.errorRate = errorRate;
    }

    @Override
    public String toString() {
        return "RequestCountData{"
                + "requestNum=" + requestNum
                + ", requestFailNum=" + requestFailNum
                + ", errorRate=" + errorRate
                + '}';
    }
}
