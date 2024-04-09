/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.implement.service.dynamicconfig.kie.client.kie;

import org.apache.http.client.config.RequestConfig;

/**
 * Kie request
 *
 * @author zhouss
 * @since 2021-11-17
 */
public class KieRequest {
    /**
     * Label condition label=version:1.0 -> Queries kv with label version 1.0
     */
    private String labelCondition;

    /**
     * Wait time for establishing connection with kie (unit: S) During this time it waits and returns if the relevant
     * kie changes. OFFICIAL-STATEMENT： "wait until any kv changed. for example wait=5s, server will not response until
     * 5 seconds, during that time window, if any kv changed, server will return 200 and kv list, otherwise return 304
     * and empty body".
     *
     * It is recommended that the time not exceed 50 seconds. If the time exceeds 50 seconds, it will be calculated as
     * 50 seconds
     */
    private String wait;

    /**
     * revision If the version of the configuration center is later than the current version, new data is returned
     */
    private String revision;

    /**
     * http request configuration
     */
    private RequestConfig requestConfig;

    /**
     * accurateMatchLabel true : Precisely match the kv of the label false : Match all kv that contain the label
     * condition
     */
    private boolean accurateMatchLabel = true;

    public RequestConfig getRequestConfig() {
        return requestConfig;
    }

    public void setRequestConfig(RequestConfig requestConfig) {
        this.requestConfig = requestConfig;
    }

    public String getLabelCondition() {
        return labelCondition;
    }

    /**
     * set label condition
     *
     * @param labelCondition label condition
     * @return KieRequest
     */
    public KieRequest setLabelCondition(String labelCondition) {
        this.labelCondition = labelCondition;
        return this;
    }

    public boolean isAccurateMatchLabel() {
        return accurateMatchLabel;
    }

    public void setAccurateMatchLabel(boolean accurateMatchLabel) {
        this.accurateMatchLabel = accurateMatchLabel;
    }

    public String getWait() {
        return wait;
    }

    /**
     * set wait
     *
     * @param wait wait time
     * @return KieRequest
     */
    public KieRequest setWait(String wait) {
        this.wait = wait;
        return this;
    }

    public String getRevision() {
        return revision;
    }

    /**
     * set revision
     *
     * @param revision revision
     * @return KieRequest
     */
    public KieRequest setRevision(String revision) {
        this.revision = revision;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        KieRequest that = (KieRequest) obj;

        return labelCondition != null ? labelCondition.equals(that.labelCondition) : that.labelCondition == null;
    }

    @Override
    public int hashCode() {
        return labelCondition != null ? labelCondition.hashCode() : 0;
    }
}
