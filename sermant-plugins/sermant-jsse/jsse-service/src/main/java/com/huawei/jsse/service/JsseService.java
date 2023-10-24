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

package com.huawei.jsse.service;

import com.huawei.jsse.common.Constants;
import com.huawei.jsse.config.JsseConfig;
import com.huawei.jsse.entity.JsseLinkInfo;
import com.huawei.jsse.entity.JsseRpcInfo;
import com.huawei.jsse.manager.JsseManager;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginService;
import com.huaweicloud.sermant.core.utils.StringUtils;

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
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JSSE服务类，上报指标数据
 *
 * @author zhp
 * @since 2023-10-17
 */
public class JsseService implements PluginService {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final JsseConfig JSSE_CONFIG = PluginConfigManager.getPluginConfig(JsseConfig.class);

    private static final int CONTAINER_NAME_INDEX = 2;

    private static final int ENABLE_NUM = 2;

    /**
     * 换行符。Gopher解析时需要用到
     * System.lineSeparator()在UNIX返回"\n" 在Windows 返回"\r\n". 因此不能修改为System.lineSeparator();
     */
    private static final String LINE_BREAK = "|\r\n";

    private static final BigDecimal MULTIPLYING_POWER = new BigDecimal(1000);

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
    private String jsseFileName;

    private ScheduledExecutorService executorService;

    @Override
    public void start() {
        if (!JSSE_CONFIG.isEnable()) {
            return;
        }
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        String name = runtimeMxBean.getName();
        int index = name.indexOf(Constants.PROCESS_NAME_LINK);
        if (index != -1) {
            pid = name.substring(0, index);
            processName = name.substring(index + 1);
        } else {
            pid = "-1";
            processName = StringUtils.EMPTY;
        }
        initMachineId();
        initContainerInfo();
        String filePath = JSSE_CONFIG.getFilePath() + pid;
        jsseFileName = filePath + Constants.FILE_PATH_LINK + JSSE_CONFIG.getFileName();
        try {
            createTmpFile(filePath, jsseFileName);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "create metric file failed", e);
            return;
        }
        executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(this::writeMetricData, JSSE_CONFIG.getDelayTime(), JSSE_CONFIG.getPeriod(),
                TimeUnit.MILLISECONDS);
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
        if (JsseManager.getJsseLinkMap().isEmpty() && JsseManager.getJsseRpcMap().isEmpty()) {
            return;
        }
        try (RandomAccessFile raf = new RandomAccessFile(jsseFileName, "rw");
                FileLock lock = raf.getChannel().lock()) {
            raf.seek(raf.length());
            writeLinkData(raf);
            writeRpcData(raf);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Exception writing indicator data to file.", e);
        }
    }

    /**
     * 写连接数据
     *
     * @param raf 文件连接
     * @throws IOException 文件写入异常
     */
    private void writeLinkData(RandomAccessFile raf) throws IOException {
        for (Entry<String, JsseLinkInfo> entry : JsseManager.getJsseLinkMap().entrySet()) {
            JsseLinkInfo oldJsseInfo = entry.getValue();
            if (oldJsseInfo.getSentMessages().get() == 0 && oldJsseInfo.getReceiveMessages().get() == 0) {
                continue;
            }
            JsseLinkInfo jsseLinkInfo = JSONObject.parseObject(JSONObject.toJSONString(oldJsseInfo),
                    JsseLinkInfo.class);
            raf.write(String.format(Locale.ROOT, "|sermant_l7_link|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s",
                    pid, jsseLinkInfo.getClientIp(), jsseLinkInfo.getServerIp(), jsseLinkInfo.getServerPort(),
                    jsseLinkInfo.getL4Role(), jsseLinkInfo.getL7Role(), jsseLinkInfo.getProtocol(),
                    jsseLinkInfo.isEnableSsl() ? Constants.SSL_OPEN : Constants.SSL_CLOSE,
                    jsseLinkInfo.getSentBytes().get(), jsseLinkInfo.getSentMessages().get(),
                    jsseLinkInfo.getReceiveBytes().get(), jsseLinkInfo.getReceiveMessages().get()
            ).getBytes(Charset.defaultCharset()));
            raf.write(LINE_BREAK.getBytes(Charset.defaultCharset()));
            oldJsseInfo.getReceiveMessages().getAndAdd(-jsseLinkInfo.getReceiveMessages().get());
            oldJsseInfo.getReceiveBytes().getAndAdd(-jsseLinkInfo.getReceiveBytes().get());
            oldJsseInfo.getSentMessages().getAndAdd(-jsseLinkInfo.getSentMessages().get());
            oldJsseInfo.getSentBytes().getAndAdd(-jsseLinkInfo.getSentBytes().get());
        }
    }

    /**
     * 写RPC数据
     *
     * @param raf 文件连接
     * @throws IOException 文件写入异常
     */
    private void writeRpcData(RandomAccessFile raf) throws IOException {
        for (Entry<String, JsseRpcInfo> entry : JsseManager.getJsseRpcMap().entrySet()) {
            JsseRpcInfo oldJsseInfo = entry.getValue();
            if (oldJsseInfo.getReqCount().get() == 0 && oldJsseInfo.getResponseCount().get() == 0) {
                continue;
            }
            JsseRpcInfo jsseRpcInfo = JSONObject.parseObject(JSONObject.toJSONString(oldJsseInfo),
                    JsseRpcInfo.class);
            BigDecimal reqCount = new BigDecimal(jsseRpcInfo.getReqCount().get());
            BigDecimal resCount = new BigDecimal(jsseRpcInfo.getResponseCount().get());
            BigDecimal errorCount = new BigDecimal(jsseRpcInfo.getReqErrorCount().get());
            BigDecimal errorRatio = errorCount.divide(reqCount, ENABLE_NUM, RoundingMode.HALF_UP);
            BigDecimal sumLatency = new BigDecimal(jsseRpcInfo.getSumLatency().get());
            BigDecimal avgLatency = sumLatency.divide(reqCount, ENABLE_NUM, RoundingMode.HALF_UP);
            BigDecimal reqThroughout = reqCount.multiply(MULTIPLYING_POWER).divide(avgLatency, ENABLE_NUM,
                    RoundingMode.HALF_UP);
            BigDecimal resThroughout = resCount.multiply(MULTIPLYING_POWER).divide(avgLatency, ENABLE_NUM,
                    RoundingMode.HALF_UP);
            String latencyHistogram = getLatencyHistogram(jsseRpcInfo.getLatencyList());
            raf.write(String.format(Locale.ROOT, "|sermant_l7_rpc|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s"
                            + "|%s|%s|%s", pid, jsseRpcInfo.getClientIp(), jsseRpcInfo.getServerIp(),
                    jsseRpcInfo.getServerPort(), jsseRpcInfo.getL4Role(), jsseRpcInfo.getL7Role(),
                    jsseRpcInfo.getProtocol(), jsseRpcInfo.isEnableSsl() ? Constants.SSL_OPEN : Constants.SSL_CLOSE,
                    reqThroughout, resThroughout, reqCount, resCount, avgLatency, latencyHistogram,
                    sumLatency, errorRatio, errorCount).getBytes(Charset.defaultCharset()));
            raf.write(LINE_BREAK.getBytes(Charset.defaultCharset()));
            oldJsseInfo.getReqErrorCount().getAndAdd(-jsseRpcInfo.getReqErrorCount().get());
            oldJsseInfo.getSumLatency().getAndAdd(-jsseRpcInfo.getSumLatency().get());
            oldJsseInfo.getResponseCount().getAndAdd(-jsseRpcInfo.getResponseCount().get());
            oldJsseInfo.getReqCount().getAndAdd(-jsseRpcInfo.getReqCount().get());
            oldJsseInfo.getLatencyList().removeAll(jsseRpcInfo.getLatencyList());
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
            LOGGER.log(Level.WARNING, "Error executing command: dmidecode -s system-uuid.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Command execution interrupted.", e);
        }
        machineId = StringUtils.EMPTY;
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
            LOGGER.log(Level.WARNING, "Failed to get container ID.", e);
        }
        containerId = StringUtils.EMPTY;
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
            latencyHistogram.append(" ").append(RANGE[index + 1]).append(" ").append((int) latencyList.stream().filter(
                    v -> v > RANGE[latencyIndex] && v < RANGE[latencyIndex + 1]).count());
        }
        return latencyHistogram.toString();
    }
}
