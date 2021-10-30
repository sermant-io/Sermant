/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 文件夹参数
 *
 * @since 2021-10-30
 */
@Data
public class FolderParam {
    private int folderId;

    @NotBlank(message = "{folder.submit.notnull}")
    @JsonProperty("submit_info")
    private String submitInfo;

    @NotBlank(message = "{folder.name.notnull}")
    @JsonProperty("folder_name")
    private String folderName;

    /**
     * 父文件夹ID
     */
    @JsonProperty("folder_id")
    private int parentId;

    @JsonProperty("user_name")
    private String userName;
}
