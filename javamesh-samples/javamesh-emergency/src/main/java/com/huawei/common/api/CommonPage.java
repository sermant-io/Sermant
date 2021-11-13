/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.common.api;

import lombok.Data;

import java.util.List;

/**
 * 参数模板格式
 *
 * @author y30010171
 * @since 2021-11-09
 **/
@Data
public class CommonPage<ObjectType> {
    // 接收的对象
    private ObjectType object;

    // 接收对象的集合
    private List<ObjectType> objectList;

    // 页码(查询条件)
    private int pageIndex = 1;

    // 条数(查询条件)
    private int pageSize = 10;

    // 开始时间(查询条件)
    private String startTime;

    // 结束时间(查询条件)
    private String endTime;

    // 排序字段
    private String sortField;

    // 排序类型 asc正序 desc倒叙
    private String sortType;
}