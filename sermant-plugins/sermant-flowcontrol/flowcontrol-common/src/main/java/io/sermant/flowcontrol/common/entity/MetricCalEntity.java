/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package io.sermant.flowcontrol.common.entity;

/**
 * index computes entity class
 *
 * @author zhp
 * @since 2022-09-15
 */
public class MetricCalEntity {
    /**
     * total requests on the server
     */
    private long serverReqSum;

    /**
     * total successful requests on the server
     */
    private long successFulServerReqSum;

    /**
     * total request time on the server
     */
    private long consumeServerReqTimeSum;

    /**
     * total number of failed requests on the server
     */
    private long failedServerReqSum;

    public long getServerReqSum() {
        return serverReqSum;
    }

    public void setServerReqSum(long serverReqSum) {
        this.serverReqSum = serverReqSum;
    }

    public long getSuccessFulServerReqSum() {
        return successFulServerReqSum;
    }

    public void setSuccessFulServerReqSum(long successFulServerReqSum) {
        this.successFulServerReqSum = successFulServerReqSum;
    }

    public long getConsumeServerReqTimeSum() {
        return consumeServerReqTimeSum;
    }

    public void setConsumeServerReqTimeSum(long consumeServerReqTimeSum) {
        this.consumeServerReqTimeSum = consumeServerReqTimeSum;
    }

    public long getFailedServerReqSum() {
        return failedServerReqSum;
    }

    public void setFailedServerReqSum(long failedServerReqSum) {
        this.failedServerReqSum = failedServerReqSum;
    }
}
