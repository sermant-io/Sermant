package com.huawei.apm.core.lubanops.bootstrap.plugin.common;

import java.util.List;
import java.util.Map;

import com.huawei.apm.core.lubanops.bootstrap.collector.api.MonitorDataRow;
import com.huawei.apm.core.lubanops.bootstrap.collector.api.SinglePrimaryKeyAggregator;
import com.huawei.apm.core.lubanops.bootstrap.collector.interceptor.SQLAroundInterceptor;
import com.huawei.apm.core.lubanops.bootstrap.exception.ApmRuntimeException;
import com.huawei.apm.core.lubanops.bootstrap.trace.TraceCollector;
import com.huawei.apm.core.lubanops.bootstrap.utils.Util;

/**
 * 默认的SQL单主键指标集聚合器
 */
public class DefaultSqlStatsAggregator extends SinglePrimaryKeyAggregator<DefaultSqlSectionStats>
        implements SQLAroundInterceptor {
    private static ThreadLocal<DefaultSqlSectionStats> statsLocal = new ThreadLocal<DefaultSqlSectionStats>();

    private static ThreadLocal<Long> timeLocal = new ThreadLocal<Long>();

    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public Map<String, List<MonitorDataRow>> afterHarvest(List<MonitorDataRow> collected) {
        return null;
    }

    @Override
    protected Class<DefaultSqlSectionStats> getValueType() {
        return DefaultSqlSectionStats.class;
    }

    @Override
    public void parseParameters(Map<String, String> parameters) {

    }

    @Override
    public String getPrimaryKey(Map<String, String> primaryKeyMap) {
        String sql = primaryKeyMap.get("sql");
        if (sql == null) {
            throw new ApmRuntimeException("tag sql is null");
        }
        return Util.getMD5String(sql);
    }

    @Override
    protected void setPrimaryKey(MonitorDataRow row, String s) {
        row.put("md5", Util.getMD5String(s));
        row.put("sql", s);
    }

    @Override
    protected String primaryKey() {
        return null;
    }

    @Override
    public long onStart(String sql) {
        if (!isEnable) {
            return 0;
        }
        DefaultSqlSectionStats stats = this.getValue(sql);
        long start = stats.onStart();
        statsLocal.set(stats);
        timeLocal.set(start);
        return start;
    }

    @Override
    public void onThrowable(String sql, Throwable t) {
        if (!isEnable) {
            return;
        }
        DefaultSqlSectionStats stat = statsLocal.get();
        if (stat != null) {
            stat.onThrowable(sql, t);
            stat.setErrorTraceId(TraceCollector.getTraceId());
        }
    }

    @Override
    public long onFinally(String sql, int updatedRowCount, int readRowCount) {
        if (!isEnable) {
            return 0;
        }
        DefaultSqlSectionStats stat = statsLocal.get();
        Long start = timeLocal.get();
        if (stat != null && start != null) {
            long timeInNanos = System.nanoTime() - start;
            if (stat.onFinally(sql, timeInNanos, updatedRowCount, readRowCount)) {
                stat.setSlowTraceId(TraceCollector.getTraceId());
            }
            statsLocal.set(null);
            timeLocal.set(null);
            return timeInNanos;
        }
        return 0;
    }
}
