package com.huawei.javamesh.core.lubanops.bootstrap.transaction;

import java.util.ArrayList;
import java.util.List;

public class MetricTransaction {

    private String metricName;

    private List<Transaction> transactions = new ArrayList<Transaction>();

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

}
