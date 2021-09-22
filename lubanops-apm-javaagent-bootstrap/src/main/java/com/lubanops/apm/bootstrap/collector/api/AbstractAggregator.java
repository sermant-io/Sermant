package com.lubanops.apm.bootstrap.collector.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.lubanops.apm.bootstrap.config.ConfigManager;
import com.lubanops.apm.bootstrap.log.LogFactory;

/**
 * 抽象聚合类，实现一些基本指标类的封装 <br>
 * @author
 * @since 2020年3月13日
 */
public class AbstractAggregator implements MetricSetAggregator {

    protected ThreadLocal<Map<String, Object>> statsContext = new ThreadLocal<Map<String, Object>>();

    /**
     * 是否采集
     */
    protected volatile boolean isEnable = true;

    /**
     * 默认的最大行数
     */
    protected volatile int maxRowCount = ConfigManager.getMaxRow();

    /**
     * 是否已满
     */
    protected volatile boolean isFull = false;

    /**
     * 满了是否还采集
     */
    protected volatile boolean isCollectorAfterFull = false;

    /**
     * 错误消息是否已经打印出来了
     */
    protected boolean isFullOutputted = false;

    /**
     * 是否需要上报数据，默认是true
     */
    protected boolean needToUpload = true;

    /**
     * 对参数进行解析，需要的参数转成自己可以表达的类型，比如字符串参数true，转成boolean的true等
     */
    public void parseParameters(Map<String, String> parameters) {
    }

    /**
     * 对参数进行解析，需要的参数转成自己可以表达的类型，比如字符串参数true，转成boolean的true等
     */
    @Override
    public void setParameters(Map<String, String> parameters) {
        try {
            parseParameters(parameters);
        } catch (Exception e) {
            LogFactory.getLogger().log(Level.SEVERE,
                    "failed to parse parameters=" + parameters + ", error=" + e.getMessage(),
                    e);
        }
    }

    @Override
    public List<MonitorDataRow> dump() {
        return this.harvest();
    }

    @Override
    public boolean isEnable() {
        return isEnable;
    }

    @Override
    public void setEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }

    @Override
    public int getMaxRowCount() {
        return maxRowCount;
    }

    @Override
    public void setMaxRowCount(int c) {
        maxRowCount = c;
    }

    @Override
    public boolean isFull() {
        return isFull;
    }

    @Override
    public void setFull(boolean isFull) {
        this.isFull = isFull;
    }

    @Override
    public boolean isFullOutputted() {
        return isFullOutputted;
    }

    @Override
    public void setFullOutputted(boolean f) {
        this.isFullOutputted = f;
    }

    /**
     * 如果超过了行的最大值，知否还采集
     */
    @Override
    public boolean isCollectAfterFull() {
        return isCollectorAfterFull;
    }

    /**
     * @return
     */
    @Override
    public void setCollectAfterFull(boolean f) {
        isCollectorAfterFull = f;
    }

    @Override
    public boolean isNeedToUpload() {
        return needToUpload;
    }

    public void setNeedToUpload(boolean needToUpload) {
        this.needToUpload = needToUpload;
    }

    public Map<String, Object> getContextMap() {
        Map<String, Object> map = statsContext.get();
        if (map == null) {
            map = new HashMap<String, Object>();
            statsContext.set(map);
        }
        return map;
    }

    public <V> V getContextValue(Map<String, Object> map, String key) {
        if (map != null) {
            Object value = map.get(key);
            if (value != null) {
                try {
                    return (V) value;
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return null;
    }

    public void clearThreadLocal() {
        statsContext.set(null);
    }

    public Map<String, Object> getThreadLocalMap() {
        return statsContext.get();
    }

    public void setThreadLocalMap(Map<String, Object> map) {
        statsContext.set(map);
    }

    public Map<String, List<MonitorDataRow>> afterHarvest(List<MonitorDataRow> collected) {
        return null;
    }

    public MonitorDataRow getStatus(Map<String, String> primaryKeyMap) {
        return null;
    }

    public List<MonitorDataRow> getAllStatus() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public List<MonitorDataRow> harvest() {
        return null;
    }

    @Override
    public void clear() {

    }
}
