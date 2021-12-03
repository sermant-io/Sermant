/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.javamesh.core.lubanops.bootstrap.transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.AbstractPrimaryKeyValueAggregator;

public class TransactionManager {

    @SuppressWarnings("rawtypes")
    private static Map<String, AbstractPrimaryKeyValueAggregator> aggregatorMap = new HashMap<String, AbstractPrimaryKeyValueAggregator>();

    private static Map<Object, Integer> transactionMap = new HashMap<Object, Integer>();

    public static Map<Object, Integer> getTransactionMap() {
        return transactionMap;
    }

    public static void setTransactions(List<MetricTransaction> metricTransactions) {
        for (MetricTransaction metricTransaction : metricTransactions) {
            String name = metricTransaction.getMetricName();
            @SuppressWarnings("rawtypes")
            AbstractPrimaryKeyValueAggregator aggregator = aggregatorMap.get(name);
            if (aggregator != null) {
                for (Transaction transaction : metricTransaction.getTransactions()) {
                    @SuppressWarnings("unchecked")
                    Object primaryKey = aggregator.getPrimaryKey(transaction.getPrimaryKeyMap());
                    if (primaryKey != null) {
                        transactionMap.put(primaryKey, transaction.getId());
                    }
                }
            }
        }
    }

    public static void registerAggregator(String name,
            @SuppressWarnings("rawtypes") AbstractPrimaryKeyValueAggregator aggregator) {
        aggregatorMap.put(name, aggregator);
    }

}
