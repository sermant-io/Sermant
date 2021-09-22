package com.lubanops.apm.bootstrap.plugin.apm;

import java.util.List;
import java.util.Map;

import com.lubanops.apm.bootstrap.collector.api.AbstractAggregator;
import com.lubanops.apm.bootstrap.collector.api.MonitorDataRow;

public class TransferAggregator extends AbstractAggregator {

    @Override
    public void clear() {
    }

    @Override
    public String getName() {
        return "transfer";
    }

    @Override
    public List<MonitorDataRow> harvest() {
        return null;
    }

    @Override
    public void parseParameters(Map<String, String> parameters) {

    }
}
