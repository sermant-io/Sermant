/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.res4j.windows;

import com.huawei.flowcontrol.common.config.CommonConst;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.util.TimerTask;

/**
 * 滑动窗口定时任务
 *
 * @author xuezechao1
 * @since 2022-12-07
 */
public class SystemStatusTask extends TimerTask {

    private final SystemStatus systemStatus = SystemStatus.getInstance();

    @Override
    public void run() {
        // 新一轮窗口初始化数据
        if (WindowsArray.INSTANCE.calculateCurrentWindowsIndex() == 0) {
            initMinRtAndMaxThreadNum();
        }

        // 更新系统负载和CPU使用率
        OperatingSystemMXBean operatingSystemMxBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        systemStatus.setCurrentLoad(operatingSystemMxBean.getSystemLoadAverage());
        systemStatus.setCurrentCpuUsage(operatingSystemMxBean.getSystemCpuLoad());

        // 更新最小响应时间 最大线程数
        updateMinRtAndMaxThreadNum();

        // 更新qps 平均响应时间
        updateQpsAndAveRt();

        // 重置下一秒数据
        WindowsArray.INSTANCE.resetNextWindows();
    }

    /**
     * 检查数据项
     *
     * @param windowsBucket 数据窗口
     * @return 窗口是否有效
     */
    private boolean checkBucket(WindowsBucket windowsBucket) {
        if (windowsBucket == null || windowsBucket.success.sum() == 0
                || windowsBucket.rt.sum() == 0) {
            return false;
        }
        return true;
    }

    /**
     * 初始化最小响应时间 最大线程数
     */
    private void initMinRtAndMaxThreadNum() {
        systemStatus.setMinRt(Double.MAX_VALUE);
        systemStatus.setMaxThreadNum(Long.MIN_VALUE);
    }

    /**
     * 更新最小响应时间 最大线程数
     */
    private void updateMinRtAndMaxThreadNum() {
        WindowsBucket windowsBucket = WindowsArray.INSTANCE.getPreviousWindow();

        // 调用成功总数
        long successNum = windowsBucket.success.sum();

        // 响应时间总数
        double rt = windowsBucket.rt.sum();

        if (0 != successNum) {
            systemStatus.setMinRt(Math.min(systemStatus.getMinRt(), rt / successNum));
        }
        systemStatus.setMaxThreadNum((long) Math.max(systemStatus.getMaxThreadNum(), successNum));
    }

    /**
     * 更新qps 平均响应时间
     */
    private void updateQpsAndAveRt() {
        WindowsBucket previousWindowsBucket = WindowsArray.INSTANCE.getPreviousWindow();
        if (checkBucket(previousWindowsBucket)) {
            systemStatus.setQps(previousWindowsBucket.success.sum());
            systemStatus.setAveRt(previousWindowsBucket.rt.sum() / systemStatus.getQps());
            return;
        }
        WindowsBucket currentWindowsBucket = WindowsArray.INSTANCE.getCurrentWindow();
        if (checkBucket(currentWindowsBucket)) {
            long currentMillis = System.currentTimeMillis() % CommonConst.S_MS_UNIT;
            if (currentMillis != 0) {
                systemStatus.setQps(
                        (double) CommonConst.S_MS_UNIT * currentWindowsBucket.success.sum() / currentMillis);
                systemStatus.setAveRt(
                        (double) currentWindowsBucket.rt.sum() / currentWindowsBucket.success.sum());
            }
        }
    }
}
