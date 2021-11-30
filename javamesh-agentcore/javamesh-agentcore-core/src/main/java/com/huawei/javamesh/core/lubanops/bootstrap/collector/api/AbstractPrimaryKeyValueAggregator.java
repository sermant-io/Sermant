package com.huawei.javamesh.core.lubanops.bootstrap.collector.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import com.huawei.javamesh.core.lubanops.bootstrap.exception.ApmRuntimeException;
import com.huawei.javamesh.core.lubanops.bootstrap.plugin.common.DefaultSectionStats;

/**
 * 抽象的聚合器类，包含了数据收割的流程，以及内存键值对的收割流程 <br>
 * @author
 * @since 2020年3月9日
 */
public abstract class AbstractPrimaryKeyValueAggregator<K, V extends StatsBase> extends AbstractAggregator {

    public final static String DEFAULT_KEY = "default_key_sys";

    private static final int[] DEFAULT_RANGES = {10, 100, 500, 1000, 10000};

    protected volatile ConcurrentMap<K, V> valueStats;

    private AtomicReference<V> defaultValueRef = new AtomicReference<V>();

    private int[] ranges = getDefaultRanges();

    public AbstractPrimaryKeyValueAggregator() {
        this(16);
    }

    public AbstractPrimaryKeyValueAggregator(int i) {
        valueStats = new ConcurrentHashMap<K, V>(i);
    }

    /**
     * 监控系统调用的，定时收集监控数据.
     */
    @Override
    public List<MonitorDataRow> harvest() {
        if (valueStats.isEmpty()) {
            return null;
        }
        List<MonitorDataRow> rowList = new ArrayList<MonitorDataRow>();
        for (Map.Entry<K, V> entry : valueStats.entrySet()) {
            K pk = entry.getKey();
            V stats = entry.getValue();
            MonitorDataRow row = null;
            if (stats instanceof DefaultSectionStats) {
                row = ((DefaultSectionStats) stats).harvest(ranges);
            } else {
                row = stats.harvest();
            }

            if (row != null) {
                setPrimaryKey(row, pk);
                rowList.add(row);
            }
        }
        return rowList;
    }

    @Override
    public MonitorDataRow getStatus(Map<String, String> primaryKeyMap) {
        if (primaryKeyMap == null) {
            throw new ApmRuntimeException("primaryKeyMap is null");
        }
        K primaryKey = getPrimaryKey(primaryKeyMap);
        V value = this.obtainValue(primaryKey);
        if (value != null) {
            MonitorDataRow row = value.getStatus();
            setPrimaryKey(row, primaryKey);
            return row;
        }
        return null;
    }

    @Override
    public List<MonitorDataRow> getAllStatus() {
        List<MonitorDataRow> rowList = new ArrayList<MonitorDataRow>();
        for (Map.Entry<K, V> entry : valueStats.entrySet()) {
            K pk = entry.getKey();
            V stats = entry.getValue();
            MonitorDataRow row = stats.getStatus();
            setPrimaryKey(row, pk);
            rowList.add(row);
        }
        return rowList;
    }

    /**
     * 从参数map中获取主键
     * @param primaryKeyMap
     *            参数map
     * @return 主键
     */
    public abstract K getPrimaryKey(Map<String, String> primaryKeyMap);

    /**
     * 将主键的值填写如行 <br>
     * @param row
     * @param key
     * @author
     * @since 2020年3月18日
     */
    protected abstract void setPrimaryKey(MonitorDataRow row, K key);

    /**
     * 出世化一个用户数据类型的对象的方法
     * @return 用户类型对象
     */
    protected abstract Class<V> getValueType();

    /**
     * 当主键个数超过规定最大值的时候，由于内存有限，那么就都映射到一个默认的主键
     */
    protected abstract K defaultKey();

    /**
     * 根据主键获取用户值类型，如果获取不到就生成一个初始化的对象 此段代码是整个监控数据采集的核心代码，里面会存在一些bug，当前很难做到完美
     * @param key
     *            主键类型
     * @return 主键对应的用户数据对象
     */
    protected V getValue(K key) {
        V v = valueStats.get(key); // 如果包含了就返回
        if (v != null) {
            return v;
        }

        /**
         * 如果key值个数达到了上限
         */
        if (isFull) {
            v = defaultValueRef.get();
            if (null == v) {
                defaultValueRef.compareAndSet(null, createValueInstance());
                v = defaultValueRef.get();
                valueStats.putIfAbsent(defaultKey(), v);
            }
            return v;
        }

        /**
         * 此处因为并发的竞争，可能照成总的key的个数超过阈值，以后有办法就要改掉
         */
        v = createValueInstance();
        V oldvalue = valueStats.putIfAbsent(key, v);

        if (valueStats.size() > this.getMaxRowCount()) {
            this.setFull(true);
            this.setEnable(false);
        }

        if (oldvalue != null) {
            v = oldvalue;
        }
        return v;
    }

    /**
     * 获取内存里面的值 <br>
     * @param key
     * @return
     * @author
     * @since 2020年3月13日
     */
    protected V obtainValue(K key) {
        return valueStats.get(key);
    }

    private V createValueInstance() {
        try {
            V v = getValueType().newInstance();
            if (v instanceof DefaultSectionStats) {
                ((DefaultSectionStats) v).initRanges(ranges);
            }
            return v;
        } catch (InstantiationException e) {
            throw new RuntimeException("must have a default construct method!", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("IllegalAccessException!", e);
        }

    }

    /**
     * 去掉主键 <br>
     * @param key
     * @author
     * @since 2020年3月13日
     */
    protected void removeValue(K key) {
        valueStats.remove(key);
    }

    /**
     * 如果超过了行的最大值，知否还采集
     */
    @Override
    public boolean isCollectAfterFull() {
        return false;
    }

    public int size() {
        return valueStats.size();
    }

    /**
     * 清空数据
     * @return
     */
    @Override
    public void clear() {
        this.setFull(false);
        this.setFullOutputted(false);
        valueStats.clear();
    }

    @Override
    public String toString() {
        return getName();
    }

    public int[] getRanges() {
        return ranges;
    }

    public void setRanges(int[] ranges) {
        this.ranges = ranges;
    }

    public static int[] getDefaultRanges() {
        return DEFAULT_RANGES.clone();
    }

}
