package com.huawei.apm.bootstrap.lubanops.plugin.common;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.huawei.apm.bootstrap.lubanops.api.APIService;
import com.huawei.apm.bootstrap.lubanops.collector.api.MonitorDataRow;
import com.huawei.apm.bootstrap.lubanops.utils.HarvestUtil;
import com.huawei.apm.bootstrap.lubanops.utils.StringUtils;

/**
 * 默认针对方法的区间性能统计
 * <p>
 * 在方法执行前调用 {@code onStart} 方法； 在异常抛出后调用 {@code onThrowable} 方法； 在方法结束后调用
 * {@code onFinally} 方法； 获取数据时调用 {@code getStatus} 方法； 收割数据时调用 {@code harvest}
 * 方法
 * </p>
 */
public class DefaultSectionStats extends DefaultStats {
    private final static String STATS_PERFORMANCE_RANGES = "ranges";

    private final static int STATS_PERFORMANCE_RANGES_LEANGTH = 5;

    // 存储总数，不清空
    protected final AtomicLong range1 = new AtomicLong(0L);

    protected final AtomicLong range2 = new AtomicLong(0L);

    protected final AtomicLong range3 = new AtomicLong(0L);

    protected final AtomicLong range4 = new AtomicLong(0L);

    protected final AtomicLong range5 = new AtomicLong(0L);

    protected final AtomicLong range6 = new AtomicLong(0L);

    protected final AtomicReference<int[]> rangesAtomic = new AtomicReference<int[]>(
            new int[] {10, 100, 500, 1000, 10000});

    // 记录上一次读取的值，用于获取差值
    private volatile long range1Old;

    private volatile long range2Old;

    private volatile long range3Old;

    private volatile long range4Old;

    private volatile long range5Old;

    private volatile long range6Old;

    protected static void merge(MonitorDataRow dataRowFrom, MonitorDataRow dataRowDes) {
        DefaultStats.merge(dataRowFrom, dataRowDes);
        dataRowDes.put("range1", dataRowDes.get("range1") == null
                ? (Long) dataRowFrom.get("range1")
                : (Long) dataRowDes.get("range1") + (Long) dataRowFrom.get("range1"));
        dataRowDes.put("range2", dataRowDes.get("range2") == null
                ? (Long) dataRowFrom.get("range2")
                : (Long) dataRowDes.get("range2") + (Long) dataRowFrom.get("range2"));
        dataRowDes.put("range3", dataRowDes.get("range3") == null
                ? (Long) dataRowFrom.get("range3")
                : (Long) dataRowDes.get("range3") + (Long) dataRowFrom.get("range3"));
        dataRowDes.put("range4", dataRowDes.get("range4") == null
                ? (Long) dataRowFrom.get("range4")
                : (Long) dataRowDes.get("range4") + (Long) dataRowFrom.get("range4"));
        dataRowDes.put("range5", dataRowDes.get("range5") == null
                ? (Long) dataRowFrom.get("range5")
                : (Long) dataRowDes.get("range5") + (Long) dataRowFrom.get("range5"));
        dataRowDes.put("range6", dataRowDes.get("range6") == null
                ? (Long) dataRowFrom.get("range6")
                : (Long) dataRowDes.get("range6") + (Long) dataRowFrom.get("range6"));
    }

    public void initRanges(int[] newRanges) {
        if (newRanges != null && newRanges.length == 5) {
            rangesAtomic.set(newRanges);
            reset();
        }
    }

    public void parseRange(Map<String, String> parameters) {
        String newRangesStr = parameters.get(DefaultSectionStats.STATS_PERFORMANCE_RANGES);
        if (!StringUtils.isBlank(newRangesStr)) {
            int[] newRanges = APIService.getJsonApi().parseIntArray(newRangesStr);
            if (null != newRanges && newRanges.length == DefaultSectionStats.STATS_PERFORMANCE_RANGES_LEANGTH) {
                initRanges(newRanges);
            }
        }
    }

    @Override
    public boolean onFinally(long timeInNanos) {
        boolean flag = super.onFinally(timeInNanos);
        getRanges(timeInNanos);
        return flag;
    }

    public boolean onFinallyNoRunningCount(long timeInNanos) {
        boolean flag = super.onFinallyNoRunningCount(timeInNanos);
        getRanges(timeInNanos);
        return flag;
    }

    private void getRanges(long timeInNanos) {
        long timeInMills = timeInNanos / DefaultStats.NANO_TO_MILLI;
        int[] ranges = rangesAtomic.get();
        if (timeInMills < ranges[0]) {
            range1.incrementAndGet();
        } else if (timeInMills < ranges[1]) {
            range2.incrementAndGet();
        } else if (timeInMills < ranges[2]) {
            range3.incrementAndGet();
        } else if (timeInMills < ranges[3]) {
            range4.incrementAndGet();
        } else if (timeInMills < ranges[4]) {
            range5.incrementAndGet();
        } else {
            range6.incrementAndGet();
        }
    }

    @Override
    public MonitorDataRow getStatus() {
        MonitorDataRow row = super.getStatus();
        row.put("range1", range1.get());
        row.put("range2", range2.get());
        row.put("range3", range3.get());
        row.put("range4", range4.get());
        row.put("range5", range5.get());
        row.put("range6", range6.get());
        row.put("ranges", APIService.getJsonApi().toJSONString(rangesAtomic.get()));
        return row;
    }

    @Override
    public MonitorDataRow harvest() {
        return harvest(null);
    }

    public MonitorDataRow harvest(int[] newRanges) {
        int[] oldRanges = rangesAtomic.get();
        if (newRanges != null && newRanges.length == 5) {
            rangesAtomic.compareAndSet(oldRanges, newRanges);
        }
        MonitorDataRow row = super.harvest();
        if (row != null) {
            range1Old = HarvestUtil.getMetricCount(range1, range1Old, "range1", row);
            range2Old = HarvestUtil.getMetricCount(range2, range2Old, "range2", row);
            range3Old = HarvestUtil.getMetricCount(range3, range3Old, "range3", row);
            range4Old = HarvestUtil.getMetricCount(range4, range4Old, "range4", row);
            range5Old = HarvestUtil.getMetricCount(range5, range5Old, "range5", row);
            range6Old = HarvestUtil.getMetricCount(range6, range6Old, "range6", row);
            row.put("ranges", APIService.getJsonApi().toJSONString(oldRanges));
        }
        return row;
    }

    private void reset() {
        range1.getAndSet(0L);
        range2.getAndSet(0L);
        range3.getAndSet(0L);
        range4.getAndSet(0L);
        range5.getAndSet(0L);
        range6.getAndSet(0L);
        range1Old = 0L;
        range2Old = 0L;
        range3Old = 0L;
        range4Old = 0L;
        range5Old = 0L;
        range6Old = 0L;
    }

}
