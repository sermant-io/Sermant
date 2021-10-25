/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.apm.core.query;

import lombok.Getter;
import lombok.Setter;

/**
 * Druid指标查询条件
 *
 * @author zhouss
 * @since 2020-12-08
 **/
@Getter
@Setter
public class DruidQueryCondition {
    /**
     * 页码
     */
    private Integer pageNum;

    /**
     * 每页记录数
     */
    private Integer pageSize;

    /**
     * 名称关键字匹配
     */
    private String nameKeyword;

    /**
     * 数据库实例
     */
    private String databasePeer;
}
