package com.lubanops.apm.bootstrap.transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lubanops.apm.bootstrap.collector.api.AbstractPrimaryKeyValueAggregator;

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
