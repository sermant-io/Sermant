/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.dao.influxdb.common;

import com.influxdb.query.FluxTable;

import java.util.List;

/**
 * influxdb client 查询结果({@link FluxTable})解析器
 */
public interface FluxTableResolver {

    /**
     * 把{@link FluxTable}解析成<M>所对应类型的实体，并返回<M>类型实体集合
     * @param fluxTables 查询结果{@link FluxTable}
     * @param targetClass 待解析的目标类型Class对象
     * @param <M> 目标类型
     * @return 解析结果
     */
    <M> List<M> resolve(List<FluxTable> fluxTables, Class<M> targetClass);
}
