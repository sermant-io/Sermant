/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.entity;

import lombok.Data;

import java.sql.Timestamp;

/**
 * 文件夹实体类
 *
 * @since 2021-10-30
 */
@Data
public class FolderEntity {
    // 文件夹ID
    private int folderId;

    // 文件夹名
    private String folderName;

    // 提交信息
    private String submitInfo;

    // 用户名
    private String userName;

    // 最后修改日期
    private Timestamp updateTime;

    // 父文件夹ID
    private int parentId;

    // 类型
    private String type;
}
