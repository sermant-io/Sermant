/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.metrics.service;

import com.huawei.metrics.common.Constants;
import com.huawei.metrics.config.MetricsConfig;
import com.huawei.metrics.entity.MetricsRpcInfo;
import com.huawei.metrics.manager.MetricsManager;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginService;
import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.core.utils.ThreadFactoryUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 指标服务类，上报指标数据
 *
 * @author zhp
 * @since 2023-10-17
 */
public class MetricsService implements PluginService {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final MetricsConfig METRICS_CONFIG = PluginConfigManager.getPluginConfig(MetricsConfig.class);

    private static final int ENABLE_NUM = 2;

    /**
     * 换行符。Gopher解析时需要用到。 System.lineSeparator()在UNIX返回"\n",在Windows 返回"\r\n".因此不能修改为System.lineSeparator();
     */
    private static final String LINE_BREAK = "\r\n";

    private static final BigDecimal MS_TO_S = new BigDecimal(1000);

    private static final BigDecimal MS_TO_NS = new BigDecimal(1000000);

    private static final BigDecimal TO_PERCENT = new BigDecimal(100);

    private static final int[] RANGE = {0, 3, 10, 50, 100, 500, 1000, 10000};

    /**
     * 进程Id
     */
    private String pid;

    /**
     * 文件名称
     */
    private String metricsFileName;

    private ScheduledExecutorService executorService;

    @Override
    public void start() {
        if (!METRICS_CONFIG.isEnable()) {
            return;
        }
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        String name = runtimeMxBean.getName();
        int index = name.indexOf(Constants.PROCESS_NAME_LINK);
        if (index != -1) {
            pid = name.substring(0, index);
        } else {
            pid = Constants.EXCEPTION_PID;
        }
        String filePath = METRICS_CONFIG.getFilePath() + pid;
        metricsFileName = filePath + Constants.FILE_PATH_LINK + METRICS_CONFIG.getFileName();
        try {
            createTmpFile(filePath, metricsFileName);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Create metric file failed.", e);
            return;
        }
        executorService = Executors.newScheduledThreadPool(1, new ThreadFactoryUtils("metrics"));
        executorService.scheduleWithFixedDelay(this::writeMetricData, METRICS_CONFIG.getDelayTime(),
                METRICS_CONFIG.getPeriod(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    /**
     * 把指标数据写进文件里
     */
    private void writeMetricData() {
        if (MetricsManager.getRpcInfoMap().isEmpty()) {
            return;
        }
        try (RandomAccessFile raf = new RandomAccessFile(metricsFileName, "rw");
                FileLock lock = raf.getChannel().lock()) {
            raf.seek(raf.length());
            reportData(raf);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Exception writing indicator data to file.", e);
        }
    }

    /**
     * 写RPC API数据
     *
     * @param raf 文件内容访问类
     * @throws IOException 文件写入异常
     */
    private void reportData(RandomAccessFile raf) throws IOException {
        Map<String, MetricsRpcInfo> rpcInfoMap = new HashMap<>();
        StringBuilder stringBuilder = new StringBuilder();
        for (Entry<String, MetricsRpcInfo> entry : MetricsManager.getRpcInfoMap().entrySet()) {
            MetricsRpcInfo metricsRpcInfo = copyRpcInfo(entry.getValue());
            if (metricsRpcInfo.getReqCount().get() == 0 || metricsRpcInfo.getResponseCount().get() == 0
                    || metricsRpcInfo.getSumLatency().get() == 0) {
                continue;
            }
            reportBaseInfo(metricsRpcInfo, raf, Constants.RPC_API_HEADER);
            raf.write((metricsRpcInfo.getUrl() + Constants.METRICS_LINK).getBytes(StandardCharsets.UTF_8));
            BigDecimal clientErrorCount = new BigDecimal(metricsRpcInfo.getClientErrorCount().get());
            BigDecimal reqCount = new BigDecimal(metricsRpcInfo.getReqCount().get());
            BigDecimal clientErrorRatio = clientErrorCount.multiply(TO_PERCENT)
                    .divide(reqCount, ENABLE_NUM, RoundingMode.HALF_UP);
            BigDecimal serverErrorCount = new BigDecimal(metricsRpcInfo.getServerErrorCount().get());
            BigDecimal serverErrorRatio = serverErrorCount.multiply(TO_PERCENT).divide(reqCount, ENABLE_NUM,
                    RoundingMode.HALF_UP);
            BigDecimal errorCount = new BigDecimal(metricsRpcInfo.getReqErrorCount().get());
            BigDecimal errorRatio = errorCount.multiply(TO_PERCENT).divide(reqCount, ENABLE_NUM, RoundingMode.HALF_UP);
            stringBuilder.append(errorCount).append(Constants.METRICS_LINK).append(clientErrorCount)
                    .append(Constants.METRICS_LINK).append(serverErrorCount)
                    .append(Constants.METRICS_LINK).append(errorRatio).append(Constants.METRICS_LINK)
                    .append(clientErrorRatio).append(Constants.METRICS_LINK)
                    .append(serverErrorRatio).append(Constants.METRICS_LINK).append(LINE_BREAK);
            reportCommonInfo(metricsRpcInfo, raf, stringBuilder.toString());
            stringBuilder.setLength(0);
            cleanReportData(metricsRpcInfo);
            countDataInfo(metricsRpcInfo, rpcInfoMap);
        }
        for (Entry<String, MetricsRpcInfo> entry : rpcInfoMap.entrySet()) {
            MetricsRpcInfo metricsRpcInfo = entry.getValue();
            if (metricsRpcInfo.getReqCount().get() == 0 || metricsRpcInfo.getResponseCount().get() == 0
                    || metricsRpcInfo.getSumLatency().get() == 0) {
                continue;
            }
            reportBaseInfo(metricsRpcInfo, raf, Constants.RPC_HEADER);
            BigDecimal reqCount = new BigDecimal(metricsRpcInfo.getReqCount().get());
            BigDecimal errorCount = new BigDecimal(metricsRpcInfo.getReqErrorCount().get());
            BigDecimal errorRatio = errorCount.multiply(TO_PERCENT).divide(reqCount, ENABLE_NUM, RoundingMode.HALF_UP);
            stringBuilder.append(errorCount).append(Constants.METRICS_LINK).append(errorRatio)
                    .append(Constants.METRICS_LINK).append(LINE_BREAK);
            reportCommonInfo(metricsRpcInfo, raf, stringBuilder.toString());
            stringBuilder.setLength(0);
        }
    }

    /**
     * 清除本次上报的数据
     *
     * @param metricsRpcInfo 指标信息
     */
    private void cleanReportData(MetricsRpcInfo metricsRpcInfo) {
        MetricsRpcInfo originalRpcInfo = MetricsManager.getRpcInfoMap().get(MetricsManager.getKey(metricsRpcInfo));
        originalRpcInfo.getReqErrorCount().getAndAdd(-metricsRpcInfo.getReqErrorCount().get());
        originalRpcInfo.getSumLatency().getAndAdd(-metricsRpcInfo.getSumLatency().get());
        originalRpcInfo.getResponseCount().getAndAdd(-metricsRpcInfo.getResponseCount().get());
        originalRpcInfo.getReqCount().getAndAdd(-metricsRpcInfo.getReqCount().get());
        originalRpcInfo.getLatencyList().removeAll(metricsRpcInfo.getLatencyList());
    }

    /**
     * 写RPC和RPC API的基础信息
     *
     * @param metricsRpcInfo 指标信息
     * @param raf 文件内容访问类
     * @param head 指标头信息
     * @throws IOException 文件写入异常
     */
    private void reportBaseInfo(MetricsRpcInfo metricsRpcInfo, RandomAccessFile raf, String head) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        String commonInfo = stringBuilder.append(Constants.METRICS_LINK).append(head)
                .append(Constants.METRICS_LINK).append(pid).append(Constants.METRICS_LINK)
                .append(metricsRpcInfo.getClientIp()).append(Constants.METRICS_LINK)
                .append(metricsRpcInfo.getServerIp()).append(Constants.METRICS_LINK)
                .append(metricsRpcInfo.getServerPort()).append(Constants.METRICS_LINK)
                .append(metricsRpcInfo.getL4Role()).append(Constants.METRICS_LINK)
                .append(metricsRpcInfo.getL7Role()).append(Constants.METRICS_LINK)
                .append(metricsRpcInfo.getProtocol()).append(Constants.METRICS_LINK).toString();
        raf.write(commonInfo.getBytes(StandardCharsets.UTF_8));
        stringBuilder.setLength(0);
    }

    /**
     * 写RPC和RPC API的公共信息
     *
     * @param metricsRpcInfo 指标信息
     * @param raf 文件内容访问类
     * @param errorInfo 错误信息
     * @throws IOException 文件写入异常
     */
    private void reportCommonInfo(MetricsRpcInfo metricsRpcInfo, RandomAccessFile raf, String errorInfo)
            throws IOException {
        BigDecimal reqCount = new BigDecimal(metricsRpcInfo.getReqCount().get());
        BigDecimal resCount = new BigDecimal(metricsRpcInfo.getResponseCount().get());
        BigDecimal sumLatency = new BigDecimal(metricsRpcInfo.getSumLatency().get());
        BigDecimal avgLatency = sumLatency.divide(resCount, ENABLE_NUM, RoundingMode.HALF_UP);
        BigDecimal reqThroughout = reqCount.multiply(MS_TO_S).divide(new BigDecimal(METRICS_CONFIG.getDelayTime()),
                ENABLE_NUM, RoundingMode.HALF_UP);
        BigDecimal resThroughout = resCount.multiply(MS_TO_S).divide(new BigDecimal(METRICS_CONFIG.getDelayTime()),
                ENABLE_NUM, RoundingMode.HALF_UP);
        String latencyHistogram = getLatencyHistogram(metricsRpcInfo.getLatencyList());
        String sslFlag = metricsRpcInfo.isEnableSsl() ? Constants.SSL_OPEN : Constants.SSL_CLOSE;
        StringBuilder stringBuilder = new StringBuilder();
        String metricsRpcInfoStr = stringBuilder.append(sslFlag).append(Constants.METRICS_LINK)
                .append(reqThroughout).append(Constants.METRICS_LINK)
                .append(resThroughout).append(Constants.METRICS_LINK).append(reqCount)
                .append(Constants.METRICS_LINK).append(resCount).append(Constants.METRICS_LINK).append(avgLatency)
                .append(Constants.METRICS_LINK).append(latencyHistogram).append(Constants.METRICS_LINK)
                .append(sumLatency).append(Constants.METRICS_LINK).append(errorInfo).toString();
        raf.write(metricsRpcInfoStr.getBytes(StandardCharsets.UTF_8));
        stringBuilder.setLength(0);
    }

    /**
     * 统计RPC数据，非API维度
     *
     * @param metricsRpcInfo 指标数据
     * @param rpcInfoMap 保存统计后的RPC数据
     */
    private void countDataInfo(MetricsRpcInfo metricsRpcInfo, Map<String, MetricsRpcInfo> rpcInfoMap) {
        String rpcKey = MetricsManager.getRpcKey(metricsRpcInfo);
        MetricsRpcInfo rpcInfo = rpcInfoMap.get(rpcKey);
        if (rpcInfo == null) {
            rpcInfoMap.put(MetricsManager.getRpcKey(metricsRpcInfo), metricsRpcInfo);
        } else {
            rpcInfo.getReqErrorCount().getAndAdd(metricsRpcInfo.getReqErrorCount().get());
            rpcInfo.getSumLatency().getAndAdd(metricsRpcInfo.getSumLatency().get());
            rpcInfo.getResponseCount().getAndAdd(metricsRpcInfo.getResponseCount().get());
            rpcInfo.getReqCount().getAndAdd(metricsRpcInfo.getReqCount().get());
            rpcInfo.getLatencyList().addAll(metricsRpcInfo.getLatencyList());
        }
    }

    /**
     * 创建文件
     *
     * @param filePath 文件路径
     * @param fileName 文件名称
     * @throws IOException 文件创建异常
     */
    private void createTmpFile(String filePath, String fileName) throws IOException {
        File dir = new File(filePath);
        if (!dir.exists()) {
            boolean createDirFlag = dir.mkdirs();
            LOGGER.log(Level.INFO, "The folder does not exist, created the folder {0}.",
                    createDirFlag ? "successfully" : "failed");
        }
        File file = new File(fileName);
        if (!file.exists()) {
            boolean createFileFlag = file.createNewFile();
            LOGGER.log(Level.INFO, "{0} to create indicator collection file.",
                    createFileFlag ? "Successfully" : "Failed");
        }
    }

    /**
     * 生成时延直方图
     *
     * @param latencyList 时延信息
     * @return 时延直方图
     */
    private String getLatencyHistogram(List<Long> latencyList) {
        if (latencyList == null || latencyList.isEmpty()) {
            return StringUtils.EMPTY;
        }
        StringBuilder latencyHistogram = new StringBuilder(String.valueOf(RANGE.length - 1));
        for (int index = 0; index < RANGE.length - 1; index++) {
            final int latencyIndex = index;
            latencyHistogram.append(" ").append(MS_TO_NS.longValue() * RANGE[index + 1]).append(" ").append(
                    (int) latencyList.stream().filter(value -> value > RANGE[0]
                            && value < (MS_TO_NS.longValue() * RANGE[latencyIndex + 1])).count());
        }
        return latencyHistogram.toString();
    }

    /**
     * 数据拷贝
     *
     * @param metricsRpcInfo 指标数据
     * @return 指标数据信息
     */
    private MetricsRpcInfo copyRpcInfo(MetricsRpcInfo metricsRpcInfo) {
        MetricsRpcInfo targetRpcInfo = new MetricsRpcInfo();
        targetRpcInfo.setProcessId(metricsRpcInfo.getProcessId());
        targetRpcInfo.setClientIp(metricsRpcInfo.getClientIp());
        targetRpcInfo.setServerIp(metricsRpcInfo.getServerIp());
        targetRpcInfo.setServerPort(metricsRpcInfo.getServerPort());
        targetRpcInfo.setL4Role(metricsRpcInfo.getL4Role());
        targetRpcInfo.setL7Role(metricsRpcInfo.getL7Role());
        targetRpcInfo.setProtocol(metricsRpcInfo.getProtocol());
        targetRpcInfo.setContainerId(metricsRpcInfo.getContainerId());
        targetRpcInfo.setComm(metricsRpcInfo.getComm());
        targetRpcInfo.setPodName(metricsRpcInfo.getPodName());
        targetRpcInfo.setPodIp(metricsRpcInfo.getPodIp());
        targetRpcInfo.setEnableSsl(metricsRpcInfo.isEnableSsl());
        targetRpcInfo.setMachineId(metricsRpcInfo.getMachineId());
        targetRpcInfo.setUrl(metricsRpcInfo.getUrl());
        targetRpcInfo.getReqCount().set(metricsRpcInfo.getReqCount().get());
        targetRpcInfo.getResponseCount().set(metricsRpcInfo.getResponseCount().get());
        targetRpcInfo.getSumLatency().set(metricsRpcInfo.getSumLatency().get());
        targetRpcInfo.getReqErrorCount().set(metricsRpcInfo.getReqErrorCount().get());
        targetRpcInfo.getClientErrorCount().set(metricsRpcInfo.getClientErrorCount().get());
        targetRpcInfo.getServerErrorCount().set(metricsRpcInfo.getServerErrorCount().get());
        targetRpcInfo.getLatencyList().addAll(metricsRpcInfo.getLatencyList());
        return targetRpcInfo;
    }
}
