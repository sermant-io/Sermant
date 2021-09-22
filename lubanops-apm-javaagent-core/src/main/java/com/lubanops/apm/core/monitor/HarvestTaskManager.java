package com.lubanops.apm.core.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Singleton;
import com.lubanops.apm.bootstrap.collector.CollectorManager;
import com.lubanops.apm.bootstrap.collector.MonitorItem;
import com.lubanops.apm.bootstrap.collector.api.Collector;
import com.lubanops.apm.bootstrap.holder.AgentServiceContainerHolder;
import com.lubanops.apm.bootstrap.log.LogFactory;
import com.lubanops.apm.core.api.IntervalTaskManager;
import com.lubanops.apm.core.executor.ExecuteRepository;
import com.lubanops.apm.integration.utils.APMThreadFactory;

/**
 * Harvest task manager.
 * @author
 */
@Singleton
public class HarvestTaskManager implements IntervalTaskManager<HarvestTask> {

    private static final Logger LOG = LogFactory.getLogger();

    /**
     * 用于定时执行的服务，由于每一次采集时间都很短，所以用一个线程就够了
     */
    private final ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1,
            new APMThreadFactory("HarvestTaskThread"));

    /**
     * 保存所有的定时运行的任务
     */
    private final Map<Integer, HarvestTask> runningTasks = new ConcurrentHashMap<Integer, HarvestTask>();

    /**
     * 用于定时执行的服务，由于每一次采集时间都很短，所以用一个线程就够了
     */
    private AtomicBoolean onstartReport = new AtomicBoolean(false);

    @Override
    public void register(HarvestTask task, int interval) {
        this.runningTasks.put(interval, task);
    }

    @Override
    public HarvestTask unRegister(int interval) {
        return runningTasks.remove(interval);
    }

    @Override
    public HarvestTask getTask(int interval) {
        return runningTasks.get(interval);
    }

    @Override
    public Map<Integer, HarvestTask> getAllTask() {
        return runningTasks;
    }

    @Override
    public void stopTask(int interval) {

    }

    @Override
    public void stopAllTasks() {
        try {
            Set<Entry<Integer, HarvestTask>> ss = this.runningTasks.entrySet();
            Iterator<Entry<Integer, HarvestTask>> it = ss.iterator();
            while (it.hasNext()) {
                Entry<Integer, HarvestTask> entry = it.next();
                HarvestTask task = entry.getValue();
                ScheduledFuture<?> s = task.getScheduledFuture();
                if (s != null) {
                    s.cancel(false);
                }
            }
            scheduler.shutdown();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "failed to shut down", e);
        }
    }

    /**
     * 设置监控项配置.
     * @param monitorConfigList
     */
    public void setMonitorConfigList(List<MonitorItem> monitorConfigList) {

        Set<Integer> keySet = runningTasks.keySet();
        Set<Integer> oldKeySet = new HashSet<Integer>();
        oldKeySet.addAll(keySet);
        Map<Integer, List<MonitorItem>> toAdd = new HashMap<Integer, List<MonitorItem>>();
        Map<Integer, List<MonitorItem>> toMaintain = new HashMap<Integer, List<MonitorItem>>();
        Map<Integer, List<MonitorItem>> toDelete = new HashMap<Integer, List<MonitorItem>>();
        // 任务分类
        distinguishTasks(monitorConfigList, oldKeySet, toAdd, toMaintain, toDelete);
        Map<String, Collector> allCollectors = CollectorManager.newCopyOfCollectors();
        // new tasks
        newTasks(toAdd, allCollectors);
        // maintain tasks
        maintainTasks(toMaintain, allCollectors);
        // 剩下需要disable的采集器
        for (Collector c : allCollectors.values()) {
            c.setEnable(false);
        }
        // 删除任务
        deleteTasks(toDelete);

        // 启动时需要采集的采集器进行采集
        if (onstartReport.compareAndSet(false, true)) {
            collectOnstart();
        }

    }

    private void distinguishTasks(List<MonitorItem> monitorConfigList, Set<Integer> oldKeySet,
            Map<Integer, List<MonitorItem>> toAdd, Map<Integer, List<MonitorItem>> toMaintain,
            Map<Integer, List<MonitorItem>> toDelete) {
        Map<Integer, List<MonitorItem>> timeIntervalMap = new HashMap<Integer, List<MonitorItem>>();
        for (MonitorItem monitorItemApp : monitorConfigList) {
            if (monitorItemApp.getStatus() == 0) {
                List<MonitorItem> taskList = timeIntervalMap.get(monitorItemApp.getInterval());
                if (taskList == null) {
                    taskList = new LinkedList<MonitorItem>();
                    timeIntervalMap.put(monitorItemApp.getInterval(), taskList);
                }
                taskList.add(monitorItemApp);
            }
        }
        Set<Entry<Integer, List<MonitorItem>>> currentTaskSet = timeIntervalMap.entrySet();
        Iterator<Entry<Integer, List<MonitorItem>>> it = currentTaskSet.iterator();
        while (it.hasNext()) {
            Entry<Integer, List<MonitorItem>> entry = it.next();
            Integer interval = entry.getKey();
            List<MonitorItem> vv = entry.getValue();
            if (oldKeySet.contains(interval)) {
                toMaintain.put(interval, vv);
            } else {
                toAdd.put(interval, vv);
            }
            oldKeySet.remove(interval);
        }
        for (Integer interval : oldKeySet) {
            List<MonitorItem> ll = Collections.emptyList();
            toDelete.put(interval, ll);
        }
    }

    private void newTasks(Map<Integer, List<MonitorItem>> toAdd, Map<String, Collector> allCollectors) {
        // 添加新的定时任务
        Set<Entry<Integer, List<MonitorItem>>> toAddset = toAdd.entrySet();
        for (Entry<Integer, List<MonitorItem>> ee : toAddset) {
            Integer interval = ee.getKey();
            List<MonitorItem> cList = ee.getValue();

            HarvestTask newTask = AgentServiceContainerHolder.get().getService(HarvestTask.class);
            newTask.setMonitorConfigList(cList);

            ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(newTask, interval, interval, TimeUnit.SECONDS);
            newTask.setScheduledFuture(future);
            runningTasks.put(interval, newTask);
            for (MonitorItem c : cList) {
                allCollectors.remove(c.getCollectorName());
            }
        }
    }

    private void maintainTasks(Map<Integer, List<MonitorItem>> toMaintain, Map<String, Collector> allCollectors) {
        // 维持任务
        Set<Entry<Integer, List<MonitorItem>>> toMaintainSet = toMaintain.entrySet();
        for (Entry<Integer, List<MonitorItem>> ee : toMaintainSet) {
            Integer interval = ee.getKey();
            List<MonitorItem> cList = ee.getValue();

            HarvestTask old = this.getTask(interval);
            if (old != null) {
                old.setMonitorConfigList(cList);
            }
            // 移除enable的
            for (MonitorItem c : cList) {
                allCollectors.remove(c.getCollectorName());
            }
        }
    }

    private void deleteTasks(Map<Integer, List<MonitorItem>> toDelete) {
        Set<Entry<Integer, List<MonitorItem>>> toDeleteSet = toDelete.entrySet();
        for (Entry<Integer, List<MonitorItem>> ee : toDeleteSet) {
            Integer interval = ee.getKey();

            HarvestTask old = this.unRegister(interval);
            if (old != null) {
                ScheduledFuture<?> future = old.getScheduledFuture();
                if (future != null) {
                    future.cancel(false);
                }
            }
        }
    }

    private void collectOnstart() {
        List<MonitorItem> collectorOnstart = new ArrayList<MonitorItem>();
        for (Collector c : CollectorManager.getAllCollectors()) {
            if (c.isCollectOnStart()) {
                MonitorItem monitorItemApp = CollectorManager.getMonitorItemMap().get(c.getCollectorName());
                if (monitorItemApp != null) {
                    collectorOnstart.add(monitorItemApp);
                }
            }
        }
        if (!collectorOnstart.isEmpty()) {
            try {
                ExecutorService executorService = AgentServiceContainerHolder.get()
                        .getService(ExecuteRepository.class)
                        .getSharedExecutor();
                HarvestTask onstartTask = AgentServiceContainerHolder.get().getService(HarvestTask.class);
                onstartTask.setMonitorConfigList(collectorOnstart);
                executorService.submit(onstartTask);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "数据序列化失败", e);
            }
        }
    }
}
