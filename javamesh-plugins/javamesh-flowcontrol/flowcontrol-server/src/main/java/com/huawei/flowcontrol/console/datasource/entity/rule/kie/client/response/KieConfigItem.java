/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.flowcontrol.console.datasource.entity.rule.kie.client.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * kie中的配置信息
 *
 * @author Sherlockhan
 * @since 2020-12-21
 */
@Data
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
    private KieConfigLabel labels;
}
