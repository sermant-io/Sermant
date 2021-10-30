/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 场景列表信息
 *
 * @since 2021-10-30
 */
@Data
@AllArgsConstructor
public class ListHistoryParam {
    private String keywords;

    private String[] sceneName;

    private String[] sceneUser;

    private String userName;

    // 分页大小
    private int pageSize;

    // 分页数
    private int current;

    // 排序字段
    private String sorter;

    // 升序降序
    private String order;
}
