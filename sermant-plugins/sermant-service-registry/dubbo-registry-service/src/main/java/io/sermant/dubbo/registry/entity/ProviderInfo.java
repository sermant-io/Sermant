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

package io.sermant.dubbo.registry.entity;

import java.util.List;

/**
 * dubbo provider information
 *
 * @author provenceee
 * @since 2022-04-21
 */
public class ProviderInfo {
    private String serviceName;

    private List<SchemaInfo> schemaInfos;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<SchemaInfo> getSchemaInfos() {
        return schemaInfos;
    }

    public void setSchemaInfos(List<SchemaInfo> schemaInfos) {
        this.schemaInfos = schemaInfos;
    }
}