/*
 * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol.core.datasource.kie.util.response;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * kie配置的详细信息
 *
 * @author hanpeng
 * @since 2020-10-14
 */
public class KieConfigItem {
    @JSONField(name = "id")
    private String id;

    @JSONField(name = "label_format")
    private String labelFormat;

    @JSONField(name = "key")
    private String key;

    @JSONField(name = "value")
    private String value;

    @JSONField(name = "value_type")
    private String valueType;

    @JSONField(name = "create_revision")
    private int createRevision;

    @JSONField(name = "update_revision")
    private int updateRevision;

    @JSONField(name = "status")
    private String status;

    @JSONField(name = "create_time")
    private String createTime;

    @JSONField(name = "update_time")
    private String updateTime;

    @JSONField(name = "labels")
    private KieConfigLabels labels;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabelFormat() {
        return labelFormat;
    }

    public void setLabelFormat(String labelFormat) {
        this.labelFormat = labelFormat;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public int getCreateRevision() {
        return createRevision;
    }

    public void setCreateRevision(int createRevision) {
        this.createRevision = createRevision;
    }

    public int getUpdateRevision() {
        return updateRevision;
    }

    public void setUpdateRevision(int updateRevision) {
        this.updateRevision = updateRevision;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public KieConfigLabels getLabels() {
        return labels;
    }

    public void setLabels(KieConfigLabels labels) {
        this.labels = labels;
    }
}
