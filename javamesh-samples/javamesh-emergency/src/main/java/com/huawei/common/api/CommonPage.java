/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.common.api;

import lombok.Data;

import java.util.List;

/**
 * 参数模板格式
 *
 * @param <ObjectType> 需要包装的数据类型
 * @author y30010171
 * @since 2021-11-09
 **/
@Data
public class CommonPage<ObjectType> {
    private static final int DEFAULT_SIZE = 10;
    /**
     * 接收的对象
     */
    private ObjectType object;

    /**
     * 接收对象的集合
     */
    private List<ObjectType> objectList;

    /**
     * 页码
     */
    private int pageIndex = 1;

    /**
     * 分页大小
     */
    private int pageSize = DEFAULT_SIZE;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序类型 asc正序 desc倒叙
     */
    private String sortType;
}