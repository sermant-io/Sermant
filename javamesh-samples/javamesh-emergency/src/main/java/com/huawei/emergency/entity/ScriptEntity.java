/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.sql.Timestamp;

import javax.validation.constraints.NotBlank;

/**
 * 脚本实体类
 *
 * @since 2021-10-30
 */
@Data
public class ScriptEntity {
    // 脚本ID
    private int scriptId;

    // 脚本名
    @JsonProperty("script_name")
    @NotBlank(message = "{script.name.notnull}")
    private String scriptName;

    // 提交信息
    @JsonProperty("submit_info")
    @NotBlank(message = "{script.submit.notnull}")
    private String submitInfo;

    // 脚本内容
    @NotBlank(message = "{script.context.notnull}")
    private String context;

    // 用户名
    private String userName;

    // 最后修改时间
    private Timestamp updateTime;

    // 父文件夹ID
    private int folderId;

    // 类型
    private String type;

    public ScriptEntity() {
    }

    public ScriptEntity(String scriptName, String submitInfo, String context, String userName, int folderId) {
        this.scriptName = scriptName;
        this.submitInfo = submitInfo;
        this.context = context;
        this.userName = userName;
        this.folderId = folderId;
    }
}
