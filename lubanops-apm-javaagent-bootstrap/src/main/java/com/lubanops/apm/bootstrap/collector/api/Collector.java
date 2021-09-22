package com.lubanops.apm.bootstrap.collector.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import com.lubanops.apm.bootstrap.api.APIService;
import com.lubanops.apm.bootstrap.collector.CollectorManager;
import com.lubanops.apm.bootstrap.config.ConfigManager;
import com.lubanops.apm.bootstrap.log.LogFactory;
import com.lubanops.apm.bootstrap.log.LogPathUtils;
import com.lubanops.apm.bootstrap.utils.FileUtils;

/**
 * 采集器的基类 ,里面封装了所有的采集器的基类<br>
 * @author
 * @since 2020年3月10日
 */
public abstract class Collector {

    private Map<String, MetricSetAggregator> aggrMap = new ConcurrentHashMap<String, MetricSetAggregator>();

    private boolean isEnable = false;

    private boolean isError = false;

    private int monitorItemId;

    private Map<String, String> parameters;

    public void register() {
        // 要在addModelAggregator之后执行 不写在构造函数中
        CollectorManager.register(this);
    }

    public Map<String, MetricSetAggregator> getModelAggregatorMap() {
        return aggrMap;
    }

    protected void addModelAggregator(MetricSetAggregator aggr) {
        if (aggr != null) {
            aggrMap.put(aggr.getName(), aggr);
        }
    }

    public void setMaxRow(int maxRow) {
        for (MetricSetAggregator modelAggregator : aggrMap.values()) {
            int oldMaxRow = modelAggregator.getMaxRowCount();
            modelAggregator.setMaxRowCount(maxRow);
            if (modelAggregator instanceof AbstractPrimaryKeyValueAggregator) {
                int size = ((AbstractPrimaryKeyValueAggregator) modelAggregator).size();
                if (size >= maxRow || size >= oldMaxRow) {
                    modelAggregator.clear();
                }
            } else {
                if (oldMaxRow != maxRow) {
                    modelAggregator.clear();
                }
            }
        }
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        if (parameters == null) {
            parameters = new HashMap<String, String>();
        }
        this.parameters = parameters;
        try {
            String maxRowStr = parameters.get("maxRow");
            if (maxRowStr != null) {
                int maxRow = Integer.valueOf(maxRowStr);
                if (maxRow > ConfigManager.MAX_ROW_H || maxRow < ConfigManager.MAX_ROW_L) {
                    maxRow = ConfigManager.getMaxRow();
                }
                setMaxRow(maxRow);
            } else {
                setMaxRow(ConfigManager.getMaxRow());
            }
            parseParameter(parameters);
        } catch (Exception e) {
            LogFactory.getLogger().log(Level.SEVERE, "failed to parse parameter:" + parameters, e);
        }

        for (MetricSetAggregator msa : aggrMap.values()) {
            msa.setParameters(parameters);
        }
    }

    /**
     * 解析参数 ,子类来实现<br>
     * @param parameters
     * @author
     * @since 2020年3月12日
     */
    public abstract void parseParameter(Map<String, String> parameters);

    /**
     * 采集器做一次采集
     * @return
     */
    public List<MetricSet> harvest() {
        List<MetricSet> result = new ArrayList<MetricSet>();
        for (MetricSetAggregator agg : aggrMap.values()) {
            MetricSet metricSetItem = new MetricSet();
            List<MonitorDataRow> rows = null;
            if (agg.isFull()) {
                // 主键超过最大行数
                metricSetItem.setCode(1);
                StringBuilder sb = new StringBuilder();
                sb.append("row count exceeds the limitation:");
                sb.append(agg.getMaxRowCount());
                sb.append(",model:");
                sb.append(getCollectorName());
                sb.append(".");
                sb.append(agg.getName());
                metricSetItem.setMsg(sb.toString());

                dumpFull(agg, metricSetItem); // 将日志打印到本地文件，并且放入数据的attachment

            } else {
                rows = agg.harvest();
                if (rows != null && !rows.isEmpty()) {
                    metricSetItem.setDataRows(rows);
                    metricSetItem.setName(agg.getName());
                    result.add(metricSetItem);
                }
            }
            Map<String, List<MonitorDataRow>> map = agg.afterHarvest(rows);
            if (map != null) {
                Set<Entry<String, List<MonitorDataRow>>> set = map.entrySet();
                Iterator<Entry<String, List<MonitorDataRow>>> itr = set.iterator();
                while (itr.hasNext()) {
                    MetricSet afterMetricSetItem = new MetricSet();
                    Entry<String, List<MonitorDataRow>> entry = itr.next();
                    afterMetricSetItem.setName(entry.getKey());
                    afterMetricSetItem.setDataRows(entry.getValue());
                    result.add(afterMetricSetItem);
                }
            }
        }
        return result;
    }

    private void dumpFull(MetricSetAggregator agg, MetricSet metricSetItem) {
        try {
            // 将超过最大行数的数据打到磁盘
            if (!agg.isFullOutputted()) {
                LogFactory.getLogger().warning(
                        "aggregator is full,collector:" + getCollectorName() + ",aggr:" + agg.getName() + ",maxRows:"
                                + agg.getMaxRowCount());
                agg.setFullOutputted(true);
                List<MonitorDataRow> dumped = agg.dump();
                if (dumped != null && !dumped.isEmpty()) {
                    // 发送一次
                    String attachment = APIService.getJsonApi().toJSONString(dumped);
                    metricSetItem.setAttachment(attachment);
                    FileUtils.writeFile(
                            LogPathUtils.getLogPath() + File.separator + "dumps" + File.separator + getCollectorName()
                                    + "&"
                                    + agg.getName(),
                            attachment);
                } else {
                    LogFactory.getLogger().warning(
                            "dumped is empty for collector:" + getCollectorName() + ",aggr:" + agg.getName());
                }
            }
        } catch (Exception e) {
            LogFactory.getLogger().log(Level.SEVERE,
                    "handle full error:" + getCollectorName() + ",aggr:" + agg.getName(), e);
        }
    }

    public boolean isEnable() {
        if (isError) {
            return false;
        }
        if (ConfigManager.isStopAgent()) {
            return false;
        }
        if (!ConfigManager.isValidated()) {
            return false;
        }
        return isEnable;
    }

    /**
     * 设置采集器下面的所有的聚合器的状态
     * @param flag
     */
    public void setEnable(boolean flag) {
        isEnable = flag;
        Collection<MetricSetAggregator> ll = aggrMap.values();
        if (ll != null && !ll.isEmpty()) {
            for (MetricSetAggregator agg : ll) {
                agg.setEnable(flag);
            }
        }
    }

    /**
     * 系统启动的时候是否采集一次,这种适用于采集JVMInfo信息，当系统启动的时候立即采集一次，采集间隔可以放长一些，因为每次采集都是一样的
     * @return
     */
    public boolean isCollectOnStart() {
        return false;
    }

    /**
     * 采集器的名字，由子类来实现
     * @return
     */
    public abstract String getCollectorName();

    public int getMonitorItemId() {
        return monitorItemId;
    }

    public void setMonitorItemId(int monitorItemId) {
        this.monitorItemId = monitorItemId;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean isError) {
        this.isError = isError;
    }

}
