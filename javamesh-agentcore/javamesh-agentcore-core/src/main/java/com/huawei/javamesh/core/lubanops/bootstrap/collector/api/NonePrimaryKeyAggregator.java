package com.huawei.javamesh.core.lubanops.bootstrap.collector.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/*
 * 对于没有主键的行的采集器的基类，用户只需要实现harvest()方法了
 */
public abstract class NonePrimaryKeyAggregator extends AbstractAggregator {

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean isCollectAfterFull() {
        return false;
    }

    @Override
    public MonitorDataRow getStatus(Map<String, String> primaryKeyMap) {
        List<MonitorDataRow> list = this.getAllStatus();
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    /**
     * 针对大部分 {@link NonePrimaryKeyAggregator}，由于调用 {@code harvest} 方法时不会清空内部数据，
     * 因此 {@code getAllStatus} 方法采用 {@code harvest} 的默认实现；有特殊处理的必须覆盖本方法
     *
     * @return
     */
    @Override
    public abstract List<MonitorDataRow> getAllStatus();

    @Override
    public List<MonitorDataRow> harvest() {
        MonitorDataRow row = constructItemRow();
        return (row == null) ? null : Collections.singletonList(row);
    }

    /**
     * 对于单主键，子类来实现
     *
     * @return
     */
    public abstract MonitorDataRow constructItemRow();
}
