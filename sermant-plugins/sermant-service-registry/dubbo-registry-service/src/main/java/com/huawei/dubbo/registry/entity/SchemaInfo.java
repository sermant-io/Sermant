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

package com.huawei.dubbo.registry.entity;

import java.util.Map;
import java.util.Objects;

/**
 * 契约
 *
 * @author provenceee
 * @since 2022-04-21
 */
public class SchemaInfo {
    private String schemaId;

    private String group;

    private String version;

    private Map<String, String> parameters;

    /**
     * 构造方法
     */
    public SchemaInfo() {
    }

    /**
     * 构造方法
     *
     * @param schemaId 契约
     * @param group 组
     * @param version 版本
     */
    public SchemaInfo(String schemaId, String group, String version) {
        this();
        this.schemaId = schemaId;
        this.group = group;
        this.version = version;
    }

    public String getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass()) {
            SchemaInfo that = (SchemaInfo) obj;
            return Objects.equals(schemaId, that.schemaId) && Objects.equals(group, that.group)
                && Objects.equals(version, that.version);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(schemaId, group, version);
    }
}