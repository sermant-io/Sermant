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
import com.huawei.metrics.entity.MetricsLinkInfo;
import com.huawei.metrics.entity.MetricsRpcInfo;
import com.huawei.metrics.manager.MetricsManager;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginService;
import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.core.utils.ThreadFactoryUtils;

import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.util.List;
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

    private static final int CONTAINER_NAME_INDEX = 2;

    private static final int ENABLE_NUM = 2;

    /**
     * 换行符。Gopher解析时需要用到 System.lineSeparator()在UNIX返回"\n" 在Windows 返回"\r\n". 因此不能修改为System.lineSeparator();
     */
    private static final String LINE_BREAK = "|\r\n";

    private static final BigDecimal MS_TO_S = new BigDecimal(1000);

    private static final BigDecimal MS_TO_NS = new BigDecimal(1000000);

    private static final BigDecimal TO_PERCENT = new BigDecimal(100);

    private static final int[] RANGE = {0, 3, 10, 50, 100, 500, 1000, 10000};

    /**
     * 进程Id
     */
    private String pid;

    /**
     * 进程名称
     */
    private String processName;

    /**
     * 机器Id
     */
    private String machineId;

    /**
     * 容器ID
     */
    private String containerId;

    /**
     * 容器名称
     */
    private String containerName;

    /**
     * 容器IP
     */
    private String containerIp;

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
            processName = name.substring(index + 1);
        } else {
            pid = Constants.EXCEPTION_PID;
            processName = StringUtils.EMPTY;
        }
        initMachineId();
        initContainerInfo();
        String filePath = METRICS_CONFIG.getFilePath() + pid;
        metricsFileName = filePath + Constants.FILE_PATH_LINK + METRICS_CONFIG.getFileName();
        try {
            createTmpFile(filePath, metricsFileName);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Create metric file failed.", e);
            return;
        }
        executorService = Executors.newScheduledThreadPool(1, new ThreadFactoryUtils("metrics"));
        executorService.scheduleAtFixedRate(this::writeMetricData, METRICS_CONFIG.getDelayTime(),
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
        if (MetricsManager.getLinkInfoMap().isEmpty() && MetricsManager.getRpcInfoMap().isEmpty()) {
            return;
        }
        try (RandomAccessFile raf = new RandomAccessFile(metricsFileName, "rw");
                FileLock lock = raf.getChannel().lock()) {
            raf.seek(raf.length());
            writeLinkData(raf);
            writeRpcData(raf);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Exception writing indicator data to file.", e);
        }
    }

    /**
     * 写连接数据
     *
     * @param raf 文件连接
     * @throws IOException 文件写入异常
     */
    private void writeLinkData(RandomAccessFile raf) throws IOException {
        StringBuilder stringBuilder = new StringBuilder(Constants.METRICS_LINK + Constants.LINK_HEAD);
        for (Entry<String, MetricsLinkInfo> entry : MetricsManager.getLinkInfoMap().entrySet()) {
            MetricsLinkInfo originalLinkInfo = entry.getValue();
            if (originalLinkInfo.getSentMessages().get() == 0 || originalLinkInfo.getReceiveMessages().get() == 0) {
                continue;
            }
            MetricsLinkInfo metricsLinkInfo = JSONObject.parseObject(JSONObject.toJSONString(originalLinkInfo),
                    MetricsLinkInfo.class);
            String metricsInfoStr = stringBuilder.append(Constants.METRICS_LINK).append(pid)
                    .append(Constants.METRICS_LINK).append(metricsLinkInfo.getClientIp()).append(Constants.METRICS_LINK)
                    .append(metricsLinkInfo.getServerIp()).append(Constants.METRICS_LINK)
                    .append(metricsLinkInfo.getServerPort()).append(Constants.METRICS_LINK)
                    .append(metricsLinkInfo.getL4Role()).append(Constants.METRICS_LINK)
                    .append(metricsLinkInfo.getL7Role()).append(Constants.METRICS_LINK)
                    .append(metricsLinkInfo.getProtocol()).append(Constants.METRICS_LINK)
                    .append(metricsLinkInfo.isEnableSsl() ? Constants.SSL_OPEN : Constants.SSL_CLOSE)
                    .append(Constants.METRICS_LINK).append(metricsLinkInfo.getSentBytes().get())
                    .append(Constants.METRICS_LINK).append(metricsLinkInfo.getSentMessages().get())
                    .append(Constants.METRICS_LINK)
                    .append(metricsLinkInfo.getReceiveBytes().get()).append(Constants.METRICS_LINK)
                    .append(metricsLinkInfo.getReceiveMessages().get()).toString();
            raf.write(metricsInfoStr.getBytes(Charset.defaultCharset()));
            raf.write(LINE_BREAK.getBytes(Charset.defaultCharset()));
            stringBuilder.setLength(0);
            originalLinkInfo.getReceiveMessages().getAndAdd(-metricsLinkInfo.getReceiveMessages().get());
            originalLinkInfo.getReceiveBytes().getAndAdd(-metricsLinkInfo.getReceiveBytes().get());
            originalLinkInfo.getSentMessages().getAndAdd(-metricsLinkInfo.getSentMessages().get());
            originalLinkInfo.getSentBytes().getAndAdd(-metricsLinkInfo.getSentBytes().get());
        }
    }

    /**
     * 写RPC数据
     *
     * @param raf 文件连接
     * @throws IOException 文件写入异常
     */
    private void writeRpcData(RandomAccessFile raf) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for (Entry<String, MetricsRpcInfo> entry : MetricsManager.getRpcInfoMap().entrySet()) {
            MetricsRpcInfo originalRpcInfo = entry.getValue();
            if (originalRpcInfo.getReqCount().get() == 0 || originalRpcInfo.getResponseCount().get() == 0
                    || originalRpcInfo.getSumLatency().get() == 0) {
                continue;
            }
            MetricsRpcInfo metricsRpcInfo = JSONObject.parseObject(JSONObject.toJSONString(originalRpcInfo),
                    MetricsRpcInfo.class);
            BigDecimal reqCount = new BigDecimal(metricsRpcInfo.getReqCount().get());
            BigDecimal resCount = new BigDecimal(metricsRpcInfo.getResponseCount().get());
            BigDecimal errorCount = new BigDecimal(metricsRpcInfo.getReqErrorCount().get());
            BigDecimal errorRatio = errorCount.multiply(TO_PERCENT).divide(resCount, ENABLE_NUM, RoundingMode.HALF_UP);
            BigDecimal sumLatency = new BigDecimal(metricsRpcInfo.getSumLatency().get());
            BigDecimal avgLatency = sumLatency.divide(resCount, ENABLE_NUM, RoundingMode.HALF_UP);
            BigDecimal reqThroughout = reqCount.multiply(MS_TO_S).divide(new BigDecimal(METRICS_CONFIG.getDelayTime()),
                    ENABLE_NUM, RoundingMode.HALF_UP);
            BigDecimal resThroughout = resCount.multiply(MS_TO_S).divide(new BigDecimal(METRICS_CONFIG.getDelayTime()),
                    ENABLE_NUM, RoundingMode.HALF_UP);
            String latencyHistogram = getLatencyHistogram(metricsRpcInfo.getLatencyList());
            String sslFlag = metricsRpcInfo.isEnableSsl() ? Constants.SSL_OPEN : Constants.SSL_CLOSE;
            String metricsInfoStr = stringBuilder.append(Constants.METRICS_LINK).append(Constants.RPC_HEAD)
                    .append(Constants.METRICS_LINK).append(pid).append(Constants.METRICS_LINK)
                    .append(metricsRpcInfo.getClientIp()).append(Constants.METRICS_LINK)
                    .append(metricsRpcInfo.getServerIp()).append(Constants.METRICS_LINK)
                    .append(metricsRpcInfo.getServerPort()).append(Constants.METRICS_LINK)
                    .append(metricsRpcInfo.getL4Role()).append(Constants.METRICS_LINK)
                    .append(metricsRpcInfo.getL7Role()).append(Constants.METRICS_LINK)
                    .append(metricsRpcInfo.getProtocol()).append(Constants.METRICS_LINK)
                    .append(sslFlag).append(Constants.METRICS_LINK).append(reqThroughout).append(Constants.METRICS_LINK)
                    .append(resThroughout).append(Constants.METRICS_LINK).append(reqCount)
                    .append(Constants.METRICS_LINK).append(resCount).append(Constants.METRICS_LINK).append(avgLatency)
                    .append(Constants.METRICS_LINK).append(latencyHistogram).append(Constants.METRICS_LINK)
                    .append(sumLatency).append(Constants.METRICS_LINK).append(errorRatio).append(Constants.METRICS_LINK)
                    .append(errorCount).toString();
            raf.write(metricsInfoStr.getBytes(Charset.defaultCharset()));
            raf.write(LINE_BREAK.getBytes(Charset.defaultCharset()));
            stringBuilder.setLength(0);
            originalRpcInfo.getReqErrorCount().getAndAdd(-metricsRpcInfo.getReqErrorCount().get());
            originalRpcInfo.getSumLatency().getAndAdd(-metricsRpcInfo.getSumLatency().get());
            originalRpcInfo.getResponseCount().getAndAdd(-metricsRpcInfo.getResponseCount().get());
            originalRpcInfo.getReqCount().getAndAdd(-metricsRpcInfo.getReqCount().get());
            originalRpcInfo.getLatencyList().removeAll(metricsRpcInfo.getLatencyList());
        }
    }

    /**
     * 初始化机器Id
     */
    private void initMachineId() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/sh", "-c", "dmidecode -s system-uuid");
            Process process = processBuilder.start();
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (StringUtils.isExist(line)) {
                        machineId = line.trim();
                        break;
                    }
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                LOGGER.log(Level.WARNING, "Failed to execute command: dmidecode -s system-uuid.");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error executing command: dmidecode -s system-uuid.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.SEVERE, "Command execution interrupted.", e);
        }
        if (StringUtils.isEmpty(machineId)) {
            machineId = StringUtils.EMPTY;
        }
    }

    /**
     * 初始化容器信息
     */
    private void initContainerInfo() {
        try {
            containerIp = InetAddress.getLocalHost().getHostAddress();
            ProcessBuilder processBuilder = new ProcessBuilder("cat", "/proc/self/cgroup");
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("docker") || line.contains("kubepods")) {
                    String[] parts = line.split("/");
                    containerId = parts[parts.length - 1];
                    containerName = parts[parts.length - CONTAINER_NAME_INDEX];
                    break;
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                LOGGER.log(Level.WARNING, "Failed to get container ID.");
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Failed to get container ID.", e);
        }
        if (StringUtils.isEmpty(containerId)) {
            containerId = StringUtils.EMPTY;
        }
        if (StringUtils.isEmpty(containerName)) {
            containerName = StringUtils.EMPTY;
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
            dir.mkdirs();
        }
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
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
}
