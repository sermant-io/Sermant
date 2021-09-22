/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
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
