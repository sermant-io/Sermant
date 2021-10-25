/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.drill;

import com.google.gson.Gson;
import com.huawei.apm.network.language.agent.v3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.skywalking.apm.network.common.v3.CPU;
import org.apache.skywalking.apm.network.language.agent.v3.Thread;
import org.apache.skywalking.apm.network.language.agent.v3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 基础复制器父类
 *
 * @author qinfurong
 * @since 2021-06-29
 */
public class BaseReplicator {
    public static final String COPY_FLAG = "_copy";
    public static final String COPY_FILED_NAME = "copy";
    public static final String LABEL_METRIC_VALUE_FILE_NAME = "labelMetricValue";
    public static final int TO_COPY = 1;
    public static final int NO_COPY = 0;
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseReplicator.class);
    private static Gson gson = new Gson();
    private static Map<String, Map<String, Field>> allMetricFileds = new HashMap();
    private static Map<String, Field> allMetricCopyFileds = new HashMap();
    static {
        initAllMetricFileds();
    }
    /**
     * 设置指定对象的属性值
     *
     * @param metricObj 对象
     * @param fieldName 属性名
     * @param value     属性值
     */
    public static void setFiledValue(Object metricObj, String fieldName, Object value) {
        try {
            String classKeyName = metricObj.getClass().getName();
            boolean find = false;
            Map<String, Field> fieldMap = allMetricFileds.get(classKeyName);
            if (fieldMap != null) {
                Field field = fieldMap.get(fieldName);
                if (field != null) {
                    field.set(metricObj, value);
                    find = true;
                }
            }
            // 指标的copy字段
            if (!find && BaseReplicator.COPY_FILED_NAME.equals(fieldName)) {
                Field field = allMetricCopyFileds.get(classKeyName);
                if (field == null) {
                    field = metricObj.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    allMetricCopyFileds.put(classKeyName, field);
                }
                field.set(metricObj, value);
                find = true;
            }
            if (!find) {
                LOGGER.error("Class property not found. info:" + metricObj.getClass().getName() + "." + fieldName);
            }

        } catch (IllegalAccessException e) {
            LOGGER.error("Failed to set {}.{}", metricObj.getClass().getSimpleName(), fieldName);
        } catch (NoSuchFieldException e) {
            LOGGER.error("Class property not found. info:" + metricObj.getClass().getName() + "." + fieldName);
        }
    }

    /**
     * 更新属性类型为数字型的值
     *
     * @param obj              复制对象
     * @param fieldName        属性名
     * @param type             属性类型
     * @param labelValue 更新的属性值
     */
    protected static void updateNumericFiledValue(Object obj, String fieldName, MetricLabelValueType type, Object labelValue) {
        if (obj == null || StringUtils.isBlank(fieldName) || labelValue == null) {
            return;
        }
        BigDecimal metricLabelValueBigDecimal;
        String metricLabelValue = String.valueOf(labelValue);
        Object value;
        switch (type) {
            case DOUBLE:
                metricLabelValueBigDecimal = new BigDecimal(metricLabelValue);
                value = metricLabelValueBigDecimal.doubleValue();
                break;
            case LONG:
                metricLabelValueBigDecimal = new BigDecimal(metricLabelValue);
                value = metricLabelValueBigDecimal.longValue();
                break;
            case INT:
                metricLabelValueBigDecimal = new BigDecimal(metricLabelValue);
                value = metricLabelValueBigDecimal.intValue();
                break;
            default:
                return;
        }
        if (metricLabelValueBigDecimal.compareTo(new BigDecimal(0)) <= 0) {
            return;
        }
        setFiledValue(obj, fieldName, value);
    }

    /**
     * 设置copy属性为1，标记为复制数据
     *
     * @param obj 指标对象
     */
    public static void setIsCopy(Object obj) {
        if (obj == null) {
            return;
        }
        setFiledValue(obj, COPY_FILED_NAME, TO_COPY);
    }

    /**
     * 深拷贝对象
     *
     * @param obj 被拷贝对象
     * @param <T> 泛型类型
     * @return 拷贝后的对象
     */
    public static <T> T deepCopy(T obj, Class<T> clazz) {
        return (T) gson.fromJson(gson.toJson(obj), clazz);
    }

    /**
     * 解析指标标签值
     *
     * @param content 指标标签值
     * @return 解析后Map结果集
     */
    public static Map<String, Object> parseLabelMetricValueToMap(String content) {
        Map<String, Object> metricMap = new HashMap<>();
        if (StringUtils.isBlank(content)) {
            return metricMap;
        }
        try {
            return gson.fromJson(content,Map.class);
        } catch (Exception e) {
            LOGGER.error("Failed to transfer jsonStr to map.info:" + content);
        }
        return metricMap;
    }

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    /**
     * 判断是否含有指定的属性
     * @param clazz 目标类
     * @param fileName 属性名
     * @return 结果
     */
    public static boolean hasFieldByName(Class clazz,String fileName){
        if (StringUtils.isBlank(fileName)) {
            return false;
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field f : fields) {
            if (fileName.equals(f.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取指标的copy属性值
     * @param obj 目标对象
     * @return copy属性值
     */
    public static int getCopyFlagValue(Object obj) {
        boolean hasCopyField = hasFieldByName(obj.getClass(), COPY_FILED_NAME);
        if (!hasCopyField) {
            return -1;
        }
        try {
            Field field = obj.getClass().getDeclaredField(COPY_FILED_NAME);
            field.setAccessible(true);
            return (int)field.get(obj);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            LOGGER.error("get filed value is faild.");
        }
        return 0;
    }

    private static void initAllMetricFileds(){
        try {
            initPoolMetricFileds();
            initJvmMetricFileds();
            initSegmentMetricFileds();
            initMonitorMetricFileds();
        } catch (NoSuchFieldException e) {
            LOGGER.error("Class property not found. Exception: " + e.getMessage());
        }

        // 遍历设置setAccessible
        Set<Map.Entry<String, Map<String, Field>>> allMetricFiledsEntries = allMetricFileds.entrySet();
        for (Map.Entry<String, Map<String, Field>> metricClassFields: allMetricFiledsEntries) {
            Map<String, Field> classFieldsEntries = metricClassFields.getValue();
            Set<Map.Entry<String, Field>> classFields = classFieldsEntries.entrySet();
            for (Map.Entry<String, Field> fields : classFields) {
                Field field  = fields.getValue();
                field.setAccessible(true);
            }
        }
    }

    private static void initPoolMetricFileds() throws NoSuchFieldException {
        Map<String, Field> fieldMap = new HashMap<>();
        Class<DataSourceBean> beanClass = DataSourceBean.class;
        fieldMap.put("activeCount_", beanClass.getDeclaredField("activeCount_"));
        fieldMap.put("poolingCount_", beanClass.getDeclaredField("poolingCount_"));
        fieldMap.put("maxActive_", beanClass.getDeclaredField("maxActive_"));
        allMetricFileds.put(DataSourceBean.class.getName(), fieldMap);

        fieldMap = new HashMap<>();
        Class<Instance> instanceClass = Instance.class;
        fieldMap.put("activeCount_", instanceClass.getDeclaredField("activeCount_"));
        fieldMap.put("poolingCount_", instanceClass.getDeclaredField("poolingCount_"));
        fieldMap.put("maxActive_", instanceClass.getDeclaredField("maxActive_"));
        fieldMap.put("countStatistic_", instanceClass.getDeclaredField("countStatistic_"));
        allMetricFileds.put(Instance.class.getName(), fieldMap);
    }

    private static void initJvmMetricFileds() throws NoSuchFieldException {
        Map<String, Field> fieldMap = new HashMap<>();
        Class<CPU> cpuClass = CPU.class;
        fieldMap.put("usagePercent_", cpuClass.getDeclaredField("usagePercent_"));
        allMetricFileds.put(CPU.class.getName(), fieldMap);

        fieldMap = new HashMap<>();
        Class<Memory> memoryClass = Memory.class;
        fieldMap.put("used_", memoryClass.getDeclaredField("used_"));
        fieldMap.put("max_", memoryClass.getDeclaredField("max_"));
        allMetricFileds.put(Memory.class.getName(), fieldMap);

        fieldMap = new HashMap<>();
        Class<GC> gcClass = GC.class;
        fieldMap.put("time_", gcClass.getDeclaredField("time_"));
        fieldMap.put("count_", gcClass.getDeclaredField("count_"));
        allMetricFileds.put(GC.class.getName(), fieldMap);

        fieldMap = new HashMap<>();
        Class<Thread> threadClass = Thread.class;
        fieldMap.put("liveCount_", threadClass.getDeclaredField("liveCount_"));
        fieldMap.put("daemonCount_", threadClass.getDeclaredField("daemonCount_"));
        fieldMap.put("peakCount_", threadClass.getDeclaredField("peakCount_"));
        allMetricFileds.put(Thread.class.getName(), fieldMap);

        fieldMap = new HashMap<>();
        Class<MemoryPool> memoryPoolClass = MemoryPool.class;
        fieldMap.put("used_", memoryPoolClass.getDeclaredField("used_"));
        fieldMap.put("init_", memoryPoolClass.getDeclaredField("init_"));
        fieldMap.put("max_", memoryPoolClass.getDeclaredField("max_"));
        fieldMap.put("committed_", memoryPoolClass.getDeclaredField("committed_"));
        allMetricFileds.put(MemoryPool.class.getName(), fieldMap);
    }

    private static void initSegmentMetricFileds() throws NoSuchFieldException {
        Map<String, Field> fieldMap = new HashMap<>();
        Class<SpanObject> spanObjectClass = SpanObject.class;
        fieldMap.put("endTime_", spanObjectClass.getDeclaredField("endTime_"));
        allMetricFileds.put(SpanObject.class.getName(), fieldMap);
    }

    private static void initMonitorMetricFileds() throws NoSuchFieldException {
        Map<String, Field> fieldMap = new HashMap<>();
        Class<ServerCPU> serverCPUClass = ServerCPU.class;
        fieldMap.put("user_", serverCPUClass.getDeclaredField("user_"));
        fieldMap.put("sys_", serverCPUClass.getDeclaredField("sys_"));
        fieldMap.put("wait_", serverCPUClass.getDeclaredField("wait_"));
        fieldMap.put("idle_", serverCPUClass.getDeclaredField("idle_"));
        allMetricFileds.put(ServerCPU.class.getName(), fieldMap);

        fieldMap = new HashMap<>();
        Class<ServerDisk> serverDiskClass = ServerDisk.class;
        fieldMap.put("ioRead_", serverDiskClass.getDeclaredField("ioRead_"));
        fieldMap.put("ioWrite_", serverDiskClass.getDeclaredField("ioWrite_"));
        fieldMap.put("ioBusy_", serverDiskClass.getDeclaredField("ioBusy_"));
        allMetricFileds.put(ServerDisk.class.getName(), fieldMap);

        fieldMap = new HashMap<>();
        Class<ServerMemory> serverMemoryCPUClass = ServerMemory.class;
        fieldMap.put("memoryTotal_", serverMemoryCPUClass.getDeclaredField("memoryTotal_"));
        fieldMap.put("swapCached_", serverMemoryCPUClass.getDeclaredField("swapCached_"));
        fieldMap.put("cached_", serverMemoryCPUClass.getDeclaredField("cached_"));
        fieldMap.put("buffers_", serverMemoryCPUClass.getDeclaredField("buffers_"));
        fieldMap.put("memoryUsed_", serverMemoryCPUClass.getDeclaredField("memoryUsed_"));
        allMetricFileds.put(ServerMemory.class.getName(), fieldMap);

        fieldMap = new HashMap<>();
        Class<ServerNetWork> serverNetWorkCPUClass = ServerNetWork.class;
        fieldMap.put("totalReadBytes_", serverNetWorkCPUClass.getDeclaredField("totalReadBytes_"));
        fieldMap.put("totalWriteBytes_", serverNetWorkCPUClass.getDeclaredField("totalWriteBytes_"));
        fieldMap.put("totalReadPackage_", serverNetWorkCPUClass.getDeclaredField("totalReadPackage_"));
        fieldMap.put("totalWritePackage_", serverNetWorkCPUClass.getDeclaredField("totalWritePackage_"));
        allMetricFileds.put(ServerNetWork.class.getName(), fieldMap);

        fieldMap = new HashMap<>();
        Class<IBMMemoryPool> ibmMemoryPoolCPUClass = IBMMemoryPool.class;
        fieldMap.put("init_", ibmMemoryPoolCPUClass.getDeclaredField("init_"));
        fieldMap.put("used_", ibmMemoryPoolCPUClass.getDeclaredField("used_"));
        fieldMap.put("max_", ibmMemoryPoolCPUClass.getDeclaredField("max_"));
        fieldMap.put("committed_", ibmMemoryPoolCPUClass.getDeclaredField("committed_"));
        allMetricFileds.put(IBMMemoryPool.class.getName(), fieldMap);
    }
}
