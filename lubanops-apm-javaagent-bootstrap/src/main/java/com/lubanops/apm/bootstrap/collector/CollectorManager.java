package com.lubanops.apm.bootstrap.collector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.lubanops.apm.bootstrap.collector.api.Collector;
import com.lubanops.apm.bootstrap.utils.StringUtils;

/**
 * 针对采集器进行管理 <br>
 * @author
 * @since 2020年3月12日
 */
public class CollectorManager {

    /**
     * collector map
     */
    private static final ConcurrentMap<String, Collector> COLLECTOR_MAP = new ConcurrentHashMap<String, Collector>();

    private static ConcurrentMap<String, MonitorItem> monitorItemMap = new ConcurrentHashMap<String, MonitorItem>();

    public static final List<String> TAGS = new ArrayList<String>();

    public static void setMonitorItemList(List<MonitorItem> list) {
        ConcurrentMap<String, MonitorItem> newMap = new ConcurrentHashMap<String, MonitorItem>(64);
        for (MonitorItem monitorItem : list) {
            newMap.put(monitorItem.getCollectorName(), monitorItem);
        }
        CollectorManager.setMonitorItemMap(newMap);
        syncStatus();
    }

    /**
     * 注册采集器.
     * <p>
     * 如果已经注册了就抛出异常
     * @param collector
     *            collector
     */
    public static void register(Collector collector) {

        if (StringUtils.isBlank(collector.getCollectorName())) {
            return;
        }

        if (COLLECTOR_MAP.containsKey(collector.getCollectorName())) {
            return;
        }
        if (CollectorManager.getMonitorItemMap().containsKey(collector.getCollectorName())) {
            MonitorItem monitorItem = CollectorManager.getMonitorItemMap().get(collector.getCollectorName());
            if (monitorItem != null && monitorItem.getStatus() == 0) {
                collector.setParameters(monitorItem.getParameters());
                collector.setEnable(true);
            } else {
                collector.setEnable(false);
            }
        }
        COLLECTOR_MAP.put(collector.getCollectorName(), collector);
    }

    /**
     * 添加标签 <br>
     * @param tag
     *            tag name tag's collector
     * @author zWX482523
     * @since 2018年3月2日
     */
    public static void addTag(String tag) {
        if (!TAGS.contains(tag)) {
            TAGS.add(tag);
        }
    }

    /**
     * 得到所有的采集器
     * @return all collectors
     */
    public static Collection<Collector> getAllCollectors() {
        return COLLECTOR_MAP.values();
    }

    public static Collector getCollector(String name) {
        return COLLECTOR_MAP.get(name);
    }

    public static Map<String, Collector> newCopyOfCollectors() {
        return new HashMap<String, Collector>(COLLECTOR_MAP);
    }

    /**
     * sync status between collector and item.
     */
    private static void syncStatus() {
        for (Collector collector : COLLECTOR_MAP.values()) {
            MonitorItem monitorItem = CollectorManager.getMonitorItemMap().get(collector.getCollectorName());
            if (monitorItem != null && monitorItem.getStatus() == 0) {
                collector.setParameters(monitorItem.getParameters());
                collector.setEnable(true);
            } else {
                collector.setEnable(false);
            }
        }
    }

    public static ConcurrentMap<String, MonitorItem> getMonitorItemMap() {
        return monitorItemMap;
    }

    public static void setMonitorItemMap(ConcurrentMap<String, MonitorItem> monitorItemMap) {
        CollectorManager.monitorItemMap = monitorItemMap;
    }
}
