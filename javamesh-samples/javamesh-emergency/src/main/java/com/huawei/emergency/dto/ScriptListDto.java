/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.sql.Timestamp;

/**
 * 脚本列表
 *
 * @since 2021-10-30
 */
@Data
public class ScriptListDto {
    private String uid;

    // 文件夹或脚本ID
    @JsonProperty("script_id")
    private int scriptId;

    // 文件夹或脚本名
    @JsonProperty("script_name")
    private String scriptName;

    // 提交信息
    @JsonProperty("submit_info")
    private String submitInfo;

    // 创建用户
    @JsonProperty("user_name")
    private String userName;

    // 最后修改日期
    @JsonProperty("update_time")
    private Timestamp updateTime;

    // 类型
    private String type;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Timestamp getUpdateTime() {
        return updateTime;
    }
}
