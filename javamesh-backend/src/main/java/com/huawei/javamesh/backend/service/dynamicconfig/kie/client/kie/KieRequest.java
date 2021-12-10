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

package com.huawei.javamesh.backend.service.dynamicconfig.kie.client.kie;

import org.apache.http.client.config.RequestConfig;

/**
 * kie请求体
 *
 * @author zhouss
 * @since 2021-11-17
 */
public class KieRequest {
    /**
     * 标签条件
     * 例如label=version:1.0  查询含标签版本为1.0的kv
     */
    private String labelCondition;

    /**
     * 与kie建立连接等待时间，单位S
     * 在这段时间会一直等待，如果相关kie有变更则会返回
     * 官方说明：
     * "wait until any kv changed. for example wait=5s, server will not response until 5 seconds,
     * during that time window, if any kv changed, server will return 200 and kv list,
     * otherwise return 304 and empty body",
     *
     * 建议时间不超过50s,超过50s的将以50s计算
     */
    private String wait;

    /**
     * 请求版本
     * 若配置中心版本高于当前版本，则会返回新数据
     */
    private String revision;

    /**
     * http请求配置
     */
    private RequestConfig requestConfig;

    /**
     * 匹配标签
     * true : 精确匹配标签的kv
     * false : 匹配包含labelCondition的所有kv
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

    public KieRequest setWait(String wait) {
        this.wait = wait;
        return this;
    }

    public String getRevision() {
        return revision;
    }

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
