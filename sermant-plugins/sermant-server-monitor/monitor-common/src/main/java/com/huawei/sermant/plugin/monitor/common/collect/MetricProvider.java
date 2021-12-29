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

package com.huawei.sermant.plugin.monitor.common.collect;

import java.util.List;

/**
 * Metric provider
 *
 * @param <M> 指标类型
 */
public interface MetricProvider<M> {

    /**
     * 采集
     *
     * @return 采集的指标
     */
    M collect();

    /**
     * 消费已采集的指标列表
     *
     * @param metrics 已采集的指标列表
     */
    void consume(List<M> metrics);
}
