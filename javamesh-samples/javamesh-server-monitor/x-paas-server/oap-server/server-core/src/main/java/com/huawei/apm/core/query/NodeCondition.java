/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.apm.core.query;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 查询条件
 *
 * @author zhouss
 * @since 2020-11-29
 */
@Getter
@Setter
@Builder
public class NodeCondition {
    /**
     * 开始时间
     */
    private long startTime;

    /**
     * 结束时间
     */
    private long endTime;

    /**
     * 指标名称（索引或者表名）
     */
    private String metricName;

    /**
     * 指定字段查询的值集合
     */
    private List<String> columnValues;

    /**
     * 分组字段名称
     */
    private String groupByColumn;

    /**
     * 指定需要查询的字段名称
     */
    private String columnName;

    /**
     * 每页记录数
     */
    private Integer pageSize;

    /**
     * 页
     */
    private Integer pageNum;

    /**
     * 名称查找关键字
     */
    private String nameKeyWord;

    /**
     * 父节点名称
     */
    private String parentName;

    /**
     * 数据库实例
     */
    private String databasePeer;
}
