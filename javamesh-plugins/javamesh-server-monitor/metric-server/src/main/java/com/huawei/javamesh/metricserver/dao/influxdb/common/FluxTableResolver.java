/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
     *
     * @param fluxTables  查询结果{@link FluxTable}
     * @param targetClass 待解析的目标类型Class对象
     * @param <M>         目标类型
     * @return 解析结果
     */
    <M> List<M> resolve(List<FluxTable> fluxTables, Class<M> targetClass);
}
