/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 脚本列表查询入参
 *
 * @author h30009881
 * @since 2021-10-18
 */
@Data
@AllArgsConstructor
public class ScriptListParam {
    // 用户
    private String userName;

    // 模糊查询
    private String keywords;

    // 脚本名称
    private String[] scriptName;

    // 提交信息
    private String[] submitInfo;

    // 文件夹ID
    private int folderId;

    // 分页大小
    private int pageSize;

    // 分页数
    private int current;

    // 排序字段
    private String sorter;

    // 升序降序
    private String order;
}
