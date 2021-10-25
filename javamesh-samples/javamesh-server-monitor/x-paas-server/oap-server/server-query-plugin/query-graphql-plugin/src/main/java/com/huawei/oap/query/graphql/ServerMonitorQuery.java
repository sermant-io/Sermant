/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.oap.query.graphql;

import com.huawei.apm.core.query.DiskIoMetric;
import com.huawei.apm.core.query.DiskQueryCondition;
import com.huawei.skywalking.oap.server.receiver.server.monitor.collection.module.ServerMonitorModule;
import com.huawei.skywalking.oap.server.receiver.server.monitor.collection.service.ServerMonitorQueryService;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;

import org.apache.skywalking.oap.server.core.query.enumeration.Step;
import org.apache.skywalking.oap.server.core.query.input.Duration;
import org.apache.skywalking.oap.server.library.module.ModuleManager;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * server monitor 查询类
 *
 * @author zhengbin zhao
 * @version 1.0
 * @since 2021-05-15
 */
public class ServerMonitorQuery implements GraphQLQueryResolver {
    /**
     * disk io read索引的前缀
     */
    private static final String DISK_IO_READ_INDEX_PREFIX = "server_monitor_disk_ioread";

    /**
     * disk io write索引的前缀
     */
    private static final String DISK_IO_WRITE_INDEX_PREFIX = "server_monitor_disk_iowrite";

    /**
     * disk io busy索引的前缀
     */
    private static final String DISK_IO_BUSY_INDEX_PREFIX = "server_monitor_disk_iobusy";

    /**
     * 常量1
     */
    private static final int CONSTANT_ONE = 1;

    /**
     * 时间转化是的格式
     */
    private static final String MINUTE_DATE_FORMAT_STRING = "yyyyMMddHHmm";

    /**
     * 时间转化是的格式
     */
    private static final String DAY_DATE_FORMAT_STRING = "yyyyMMdd";

    private final ModuleManager moduleManager;

    private ServerMonitorQueryService monitorQueryService;

    public ServerMonitorQuery(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    private ServerMonitorQueryService getMonitorQueryService() {
        if (monitorQueryService == null) {
            this.monitorQueryService =
                moduleManager.find(ServerMonitorModule.MODULE_NAME)
                    .provider()
                    .getService(ServerMonitorQueryService.class);
        }
        return monitorQueryService;
    }

    /**
     * 查询disk io的read bytes
     *
     * @param condition 查询请求具体的参数
     * @param duration  查询时间参数
     * @return 查询结果
     */
    public List<DiskIoMetric> queryReadBytes(DiskQueryCondition condition, Duration duration)
        throws IOException, ParseException {
        return commonQuery(condition, duration, DISK_IO_READ_INDEX_PREFIX);
    }

    /**
     * 查询disk io的write bytes
     *
     * @param condition 查询请求具体的参数
     * @param duration  查询时间参数
     * @return 查询结果
     */
    public List<DiskIoMetric> queryWriteBytes(DiskQueryCondition condition, Duration duration)
        throws IOException, ParseException {
        return commonQuery(condition, duration, DISK_IO_WRITE_INDEX_PREFIX);
    }

    /**
     * 查询disk io的busy
     *
     * @param condition 查询请求具体的参数
     * @param duration  查询时间参数
     * @return 查询结果
     */
    public List<DiskIoMetric> queryBusy(DiskQueryCondition condition, Duration duration)
        throws IOException, ParseException {
        return commonQuery(condition, duration, DISK_IO_BUSY_INDEX_PREFIX);
    }

    /**
     * 公共查询接口
     *
     * @param condition 查询参数
     * @param duration  查询时间参数
     * @param indexName 索引名称
     * @return 返回查询结果
     * @throws ParseException
     * @throws IOException
     */
    private List<DiskIoMetric> commonQuery(DiskQueryCondition condition, Duration duration, String indexName)
        throws ParseException, IOException {
        List<DiskIoMetric> selectList = new ArrayList<>();
        Optional<List<DiskIoMetric>> diskIoMetrics = getMonitorQueryService()
            .queryDisk(getIndex(condition, duration, indexName));
        diskIoMetrics.ifPresent(selectList::addAll);
        if (selectList.isEmpty()) {
            return selectList;
        }
        selectList.removeIf(s -> s.getValueList().stream().mapToLong(Long::longValue).sum() < CONSTANT_ONE);
        return answerList(selectList);
    }

    /**
     * 校验返回结果是否合法
     *
     * @param list 查询结果
     * @return 处理结果
     */
    private List<DiskIoMetric> answerList(List<DiskIoMetric> list) {
        List<DiskIoMetric> diskIoMetricList = new ArrayList<>();
        for (DiskIoMetric metric : list) {
            if (metric.getValueList().size() == CONSTANT_ONE) {
                List<Long> valueList = metric.getValueList();
                valueList.addAll(metric.getValueList());
                metric.setValueList(valueList);
                diskIoMetricList.add(metric);
            } else {
                diskIoMetricList.add(metric);
            }
        }
        return diskIoMetricList;
    }

    /**
     * 获取符合的所有索引名称
     *
     * @param condition 实体条件
     * @param duration  日期条件
     * @param indexName 索引前缀
     * @return 返回索引集合
     * @throws ParseException
     */
    private DiskQueryCondition getIndex(DiskQueryCondition condition, Duration duration, String indexName)
        throws ParseException {
        DiskQueryCondition diskQueryCondition = new DiskQueryCondition();
        diskQueryCondition.setMetricName(indexName);
        diskQueryCondition.setServiceInstanceName(condition.getServiceInstanceName());
        diskQueryCondition.setServiceName(condition.getServiceName());
        diskQueryCondition.setStartTime(conversionTime(duration, "start"));
        diskQueryCondition.setEndTime(conversionTime(duration, "end"));
        diskQueryCondition.setTimeArr(setTimeArray(duration));
        return diskQueryCondition;
    }

    /**
     * 查询两个时间点之间的天数集合
     *
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @return 返回时间集合
     * @throws ParseException
     */
    private List<String> findDays(String beginTime, String endTime) throws ParseException {
        // 日期工具类准备
        DateFormat startFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat endFormat = new SimpleDateFormat(DAY_DATE_FORMAT_STRING);

        // 设置开始时间
        Calendar calBegin = Calendar.getInstance();
        calBegin.setTime(startFormat.parse(beginTime));

        // 设置结束时间
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(startFormat.parse(endTime));

        // 装返回的日期集合容器
        List<String> hourList = new ArrayList<String>();

        // 将第一个月添加里面去
        hourList.add(endFormat.format(calBegin.getTime()));
        Date endDate = startFormat.parse(endTime);

        // 每次循环给calBegin日期加一天，直到calBegin.getTime()时间等于dEnd
        while (endDate.after(calBegin.getTime())) {
            // 根据日历的规则，为给定的日历字段添加或减去指定的时间量
            calBegin.add(Calendar.DAY_OF_MONTH, CONSTANT_ONE);
            hourList.add(endFormat.format(calBegin.getTime()));
        }
        return hourList;
    }

    /**
     * 返回两个时间之间的小时集合
     *
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @return 返回小时的集合
     * @throws ParseException
     */
    private List<String> findHours(String beginTime, String endTime) throws ParseException {
        // 日期工具类准备
        DateFormat startFormat = new SimpleDateFormat("yyyy-MM-dd HH");
        DateFormat endFormat = new SimpleDateFormat("yyyyMMddHH");

        // 设置开始时间
        Calendar calBegin = Calendar.getInstance();
        calBegin.setTime(startFormat.parse(beginTime));

        // 设置结束时间
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(startFormat.parse(endTime));

        // 装返回的日期集合容器
        List<String> hourList = new ArrayList<String>();

        // 将第一个月添加里面去
        hourList.add(endFormat.format(calBegin.getTime()));
        Date endDate = startFormat.parse(endTime);

        // 每次循环给calBegin日期加一天，直到calBegin.getTime()时间等于dEnd
        while (endDate.after(calBegin.getTime())) {
            // 根据日历的规则，为给定的日历字段添加或减去指定的时间量
            calBegin.add(Calendar.HOUR_OF_DAY, CONSTANT_ONE);
            hourList.add(endFormat.format(calBegin.getTime()));
        }
        return hourList;
    }

    /**
     * 返回两个时间中间的分钟数集合
     *
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @return 返回时间集合
     * @throws ParseException
     */
    private List<String> findMinutes(String beginTime, String endTime) throws ParseException {
        // 日期工具类准备
        DateFormat startFormat = new SimpleDateFormat("yyyy-MM-dd HHmm");
        DateFormat endFormat = new SimpleDateFormat(MINUTE_DATE_FORMAT_STRING);

        // 设置开始时间
        Calendar calBegin = Calendar.getInstance();
        calBegin.setTime(startFormat.parse(beginTime));

        // 设置结束时间
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(startFormat.parse(endTime));

        // 装返回的日期集合容器
        List<String> minuteList = new ArrayList<String>();

        // 将第一个月添加里面去
        minuteList.add(endFormat.format(calBegin.getTime()));
        Date endDate = startFormat.parse(endTime);

        // 每次循环给calBegin日期加一天，直到calBegin.getTime()时间等于dEnd
        while (endDate.after(calBegin.getTime())) {
            // 根据日历的规则，为给定的日历字段添加或减去指定的时间量
            calBegin.add(Calendar.MINUTE, CONSTANT_ONE);
            minuteList.add(endFormat.format(calBegin.getTime()));
        }
        return minuteList;
    }

    /**
     * 根据时间条件设置时间集合
     *
     * @param duration 时间条件
     * @return 返回时间集合
     * @throws ParseException
     */
    private String[] setTimeArray(Duration duration) throws ParseException {
        List<String> list = new ArrayList<>();
        if (duration.getStep() == Step.DAY) {
            list = findDays(duration.getStart(), duration.getEnd());
        } else if (duration.getStep() == Step.HOUR) {
            list = findHours(duration.getStart(), duration.getEnd());
        } else {
            list = findMinutes(duration.getStart(), duration.getEnd());
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * 转化时间
     *
     * @param duration 时间条件
     * @param string   标识
     * @return 返回转化结果
     * @throws ParseException
     */
    private String conversionTime(Duration duration, String string) throws ParseException {
        String str;
        SimpleDateFormat df;
        SimpleDateFormat sdf = null;
        Date start = null;
        String startString = "start";
        String hourString = "yyyyMMddHH";
        if (duration.getStep() == Step.DAY) {
            df = new SimpleDateFormat("yyyy-MM-dd");
            sdf = new SimpleDateFormat(DAY_DATE_FORMAT_STRING);
            str = startString.equals(string) ? duration.getStart() : duration.getEnd();
            start = df.parse(str);
        } else if (duration.getStep() == Step.HOUR) {
            df = new SimpleDateFormat("yyyy-MM-dd HH");
            sdf = new SimpleDateFormat(hourString);
            str = startString.equals(string) ? duration.getStart() : duration.getEnd();
            start = df.parse(str);
        } else if (duration.getStep() == Step.MINUTE) {
            df = new SimpleDateFormat("yyyy-MM-dd HHmm");
            sdf = new SimpleDateFormat(MINUTE_DATE_FORMAT_STRING);
            str = startString.equals(string) ? duration.getStart() : duration.getEnd();
            start = df.parse(str);
        }
        if (sdf == null) {
            sdf = new SimpleDateFormat(MINUTE_DATE_FORMAT_STRING);
        }
        return sdf.format(start);
    }
}
