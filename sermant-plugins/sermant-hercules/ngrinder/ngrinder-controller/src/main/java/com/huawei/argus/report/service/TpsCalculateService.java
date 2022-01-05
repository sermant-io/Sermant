/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.argus.report.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 返回tps数据逻辑：
 * 执行时长不能按照测试的设置需要执行时长，而是需要使用当前时间-测试开始执行时间，所以这里分测试需要时长和实际执行时长，
 * 如果当前时间-测试开始时间已经大于测试需要执行时长，则直接按照测试需要执行时长来作为实际执行时长，
 * 计算时，先把原始采集的tps数据按照采集频率扩展为1秒的采集频率，这里会先把前面不重要的按照频率动态扩展，然后当采集个数与剩下的执行时间匹配时，
 * 再一起放入1秒采集队列，
 * 每一个tps的时间计算原则，在不满足展示区间长度时，不足的部分，时间补充为负数，tps值全部取0，
 * 1秒队列里面的数据，从第一个时间开始按照00:00:00开始计算，
 * 如果展示的区间小于队列的长度，则会从队列中某一个tps开始展示，该tps在队列的位置，相当于其执行时间秒
 *
 * @since 2021-11-27
 */
public class TpsCalculateService {
    /**
     * 日志记录
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TpsCalculateService.class);

    /**
     * 测试任务采集频率
     */
    private int testSampleInterval = 1;

    /**
     * 数据返回结果采集频率
     */
    private int resultSampleInterval = 1;

    /**
     * 测试执行时长
     */
    private long neededExecuteTime;

    /**
     * 测试实际执行时长
     */
    private long actualExecuteTime;

    /**
     * 结果要求展示时间秒数
     */
    private int resultShowTime;

    /**
     * 测试执行开始时间
     */
    private long testStartTime;

    /**
     * 测试结束时间
     */
    private long testEndTime;

    /**
     * 压测任务是否还在继续执行
     */
    private boolean isRunning;

    /**
     * Tps字符串数据
     */
    private JSONArray tpsOriginalData;

    /**
     * 一秒中采集频率的tps数据，需要根据tpsOriginalData和testSampleInterval来计算
     */
    private JSONArray oneSecondIntervalTpsData = new JSONArray();

    /**
     * 设置测试任务采样频率
     *
     * @param testSampleInterval 测试采样频率
     * @return 调用实例
     */
    public TpsCalculateService setTestSampleInterval(int testSampleInterval) {
        this.testSampleInterval = testSampleInterval;
        return this;
    }

    /**
     * 设置tps返回数据中需求的采样频率
     *
     * @param resultSampleInterval 测试采样频率
     * @return 调用实例
     */
    public TpsCalculateService setResultSampleInterval(int resultSampleInterval) {
        this.resultSampleInterval = resultSampleInterval;
        return this;
    }

    /**
     * 设置测试执行时间
     *
     * @param neededExecuteTime 测试执行时长
     * @return 调用实例
     */
    public TpsCalculateService setNeededExecuteTime(long neededExecuteTime) {
        this.neededExecuteTime = neededExecuteTime;
        return this;
    }

    /**
     * 设置需要展示的时间长度
     *
     * @param resultShowTime 需要展示的时间长度
     * @return 调用实例
     */
    public TpsCalculateService setResultShowTime(int resultShowTime) {
        this.resultShowTime = Math.abs(resultShowTime);
        return this;
    }

    /**
     * 设置需要测试任务开始时间
     *
     * @param testStartTime 测试任务开始时间
     * @return 调用实例
     */
    public TpsCalculateService setTestStartTime(long testStartTime) {
        this.testStartTime = testStartTime;
        return this;
    }

    /**
     * 设置需要测试任务结束时间
     *
     * @param testEndTime 测试任务结束时间
     * @return 调用实例
     */
    public TpsCalculateService setTestEndTime(long testEndTime) {
        this.testEndTime = testEndTime;
        return this;
    }

    /**
     * 设置需要测试任务开始时间
     *
     * @param isRunning 测试任务开始时间
     * @return 调用实例
     */
    public TpsCalculateService isRunning(boolean isRunning) {
        this.isRunning = isRunning;
        return this;
    }

    /**
     * 设置原始的tps数据，测试任务记录在文件中的tps数据
     *
     * @param tpsDataString tps采样记录的原始数据
     * @return 调用实例
     */
    public TpsCalculateService setTpsOriginalData(String tpsDataString) {
        if (StringUtils.isEmpty(tpsDataString)) {
            this.tpsOriginalData = new JSONArray();
            return this;
        }

        // 获取tps原始数据
        JSONArray tpsValues = JSON.parseArray(tpsDataString);
        if (tpsValues == null || tpsValues.isEmpty()) {
            this.tpsOriginalData = new JSONArray();
            return this;
        }

        // 先把tps中为null的转换成前一个值
        for (int i = 0; i < tpsValues.size(); i++) {
            Object value = tpsValues.get(i);
            if (value == null) {
                tpsValues.set(i, i > 0 ? tpsValues.get(i - 1) : 0);
            }
        }
        this.tpsOriginalData = tpsValues;
        return this;
    }

    /**
     * 把原始tps数据按照采集频率封装成1秒钟采集频率的新tps数据,个数刚好是执行时间秒数
     */
    private void setOneSecondIntervalTpsData() {
        JSONArray oneSecondIntervalTps = new JSONArray();
        if (testSampleInterval < 1) {
            return;
        }
        if (tpsOriginalData == null || tpsOriginalData.isEmpty()) {
            return;
        }

        // 测试任务换算成秒
        long testExecuteTimeSeconds = actualExecuteTime / 1000;

        // 计算如果按照1秒采集总共需要采集多少个tps数据
        long totalSampleCount = (long) tpsOriginalData.size() * testSampleInterval;
        long needSupplyCount = testExecuteTimeSeconds - totalSampleCount;

        // 当1秒采样个数小于测试时长时，在1秒采样数组前面部分补0
        while (needSupplyCount > 0) {
            oneSecondIntervalTps.add(0);
            needSupplyCount--;
        }

        // 如果测试任务的采集频率是1，那么原始数据就是1秒钟采集频率的tps数据
        if (testSampleInterval == 1) {
            this.oneSecondIntervalTpsData.addAll(tpsOriginalData);
            return;
        }

        // 如果执行时间小于等于原始数据个数，则直接把原始数据作为1秒的数据处理，这里暂未明确nGrinder后端采集原理，可能导致这个情况出现
        if (actualExecuteTime <= tpsOriginalData.size()) {
            long startIndex = tpsOriginalData.size() - actualExecuteTime;
            for (int i = (int) startIndex; i < tpsOriginalData.size(); i++) {
                oneSecondIntervalTpsData.add(tpsOriginalData.get(i));
            }
            return;
        }

        // 正常情况下执行时间应该是大于等于tps原数据个数的，所以填充正常的采集数据值
        for (int i = 0; i < tpsOriginalData.size(); i++) {

            // 还剩下多少个数据没处理
            long notHandleCount = tpsOriginalData.size() - i - 1;

            // 按照测试样本采集频率填充数据
            for (int j = 0; j < testSampleInterval; j++) {

                // 处理一个数据
                oneSecondIntervalTps.add(tpsOriginalData.get(i));

                // 还需要添加多少个1秒采集样本
                long neededAddedCount = testExecuteTimeSeconds - oneSecondIntervalTps.size();

                // 如果剩下的个数刚好能够填充1秒样本，则直接添加，不需要再按照测试任务采集频率处理
                if (neededAddedCount <= notHandleCount) {
                    break;
                }
            }
        }
        this.oneSecondIntervalTpsData.addAll(oneSecondIntervalTps);
    }

    /**
     * 设置测试到当前时间截止时，实际已经执行的时间毫秒数
     */
    private void setActualExecuteTime() {
        if (isRunning && testStartTime > 0) {
            this.actualExecuteTime = System.currentTimeMillis() - testStartTime;
            return;
        }
        if (testStartTime > 0 && testEndTime > testStartTime) {
            this.actualExecuteTime = testEndTime - testStartTime;
            return;
        }
        this.actualExecuteTime = neededExecuteTime;
    }

    /**
     * 采集数据
     *
     * @return 请求需要展示的时长中的指定频率的数据
     */
    public List<Map<String, Object>> sampleData() {
        if (!isRunning && tpsOriginalData.isEmpty()) {
            LOGGER.error("The test has not tps data.");
            return Collections.emptyList();
        }

        // 设置测试任务实际执行时长
        setActualExecuteTime();

        // 设置实际执行时长中每一秒采集数据队列
        setOneSecondIntervalTpsData();

        // 开始按照请求的频率和时间长度采集数据
        if (resultShowTime == 0) {
            LOGGER.error("Display time length is not set.");
            return Collections.emptyList();
        }

        if (resultSampleInterval <= 0) {
            LOGGER.error("Display time interval is not set.");
            return Collections.emptyList();
        }

        if (oneSecondIntervalTpsData == null || oneSecondIntervalTpsData.isEmpty()) {
            LOGGER.error("Init original tps data failed..");
            return Collections.emptyList();
        }

        // 初始化数据缓存列表
        List<Map<String, Object>> tpsInfos = new ArrayList<>();

        // 从请求设置的展示时间范围开始进行每一个采集tps数据的时间设置
        if (resultShowTime > oneSecondIntervalTpsData.size()) {

            // 先计算出需要填补多少显示为0的tps数据
            int neededSupplyTime = resultShowTime - oneSecondIntervalTpsData.size();
            for (int i = 0; i < neededSupplyTime; i++) {
                tpsInfos.add(getTpsInfo(i - neededSupplyTime, 0));
            }

            // 填补为0的数据之后，再填补实际的tps数据
            tpsInfos.addAll(sampleData(0));
        } else {
            tpsInfos.addAll(sampleData(oneSecondIntervalTpsData.size() - resultShowTime));
        }
        return tpsInfos;
    }

    /**
     * 从start开始把oneSecondIntervalTpsData的tps数据封装成map结构返回
     *
     * @param start 数据在数据组中开始索引
     * @return oneSecondIntervalTpsData的tps数据封装成map结构
     */
    private List<Map<String, Object>> sampleData(int start) {
        // 初始化数据缓存列表
        List<Map<String, Object>> tpsInfos = new ArrayList<>();

        // 填补为0的数据之后，再填补实际的tps数据
        for (int i = start; i < oneSecondIntervalTpsData.size(); i++) {
            if (i % resultSampleInterval != 0) {
                continue;
            }
            tpsInfos.add(getTpsInfo(i, oneSecondIntervalTpsData.get(i)));
        }
        return tpsInfos;
    }

    /**
     * 把TPS和时间封装成map结构，方便返回
     *
     * @param seconds  时间秒数
     * @param tpsValue tps值
     * @return map结构数据
     */
    private Map<String, Object> getTpsInfo(int seconds, Object tpsValue) {
        Map<String, Object> tpsMap = new HashMap<>(2);
        tpsMap.put("time", getTimeExpression(seconds));
        tpsMap.put("tps", tpsValue);
        return tpsMap;
    }

    /**
     * 获取时间时间表达式
     *
     * @param timeSecondsValue 时间秒数
     * @return 执行时间表达式"00:00:00"
     */
    private String getTimeExpression(int timeSecondsValue) {
        if (timeSecondsValue == 0) {
            return "00:00:00";
        }
        String format = "%s:%s:%s";
        int seconds = Math.abs(timeSecondsValue);
        String timeString = String.format(Locale.ENGLISH, format,
            getHourString(seconds), getMinuteString(seconds), getSecondsString(seconds));
        return timeSecondsValue > 0 ? timeString : "-" + timeString;
    }

    /**
     * 把秒数转换成小时表达式，设置时间中包含的整小时数量
     *
     * @param seconds 时间
     * @return 小时表达式
     */
    private String getHourString(int seconds) {
        int hour = seconds / (60 * 60);
        return hour < 10 ? "0" + hour : hour + "";
    }

    /**
     * 把秒数转换成分钟表达式，去掉小时之后的整分钟数
     *
     * @param seconds 时间秒数
     * @return 分钟表达式
     */
    private String getMinuteString(int seconds) {
        int minute = (seconds % (60 * 60)) / 60;
        return minute < 10 ? "0" + minute : minute + "";
    }

    /**
     * 把秒数转换成秒表达式，去掉小时和分钟部分后剩余的秒数
     *
     * @param seconds 时间秒数
     * @return 剩余秒数字符串
     */
    private String getSecondsString(int seconds) {
        int modSeconds = seconds % 60;
        return modSeconds < 10 ? "0" + modSeconds : modSeconds + "";
    }
}
