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

package io.sermant.flowcontrol.res4j.windows;

import com.sun.management.OperatingSystemMXBean;

import io.sermant.flowcontrol.common.config.CommonConst;

import java.lang.management.ManagementFactory;
import java.util.TimerTask;

/**
 * sliding window scheduling tasks
 *
 * @author xuezechao1
 * @since 2022-12-07
 */
public class SystemStatusTask extends TimerTask {
    private final SystemStatus systemStatus = SystemStatus.getInstance();

    @Override
    public void run() {
        // new window initialization data
        if (WindowsArray.INSTANCE.calculateCurrentWindowsIndex() == 0) {
            initMinRtAndMaxThreadNum();
        }

        // update system load and cpu usage
        OperatingSystemMXBean operatingSystemMxBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        systemStatus.setCurrentLoad(operatingSystemMxBean.getSystemLoadAverage());
        systemStatus.setCurrentCpuUsage(operatingSystemMxBean.getSystemCpuLoad());

        // Update minimum response time and maximum number of threads
        updateMinRtAndMaxThreadNum();

        // updated qps and average response time
        updateQpsAndAveRt();

        // reset next second data
        WindowsArray.INSTANCE.resetNextWindows();
    }

    /**
     * check data item
     *
     * @param windowsBucket data window
     * @return whether the window is valid
     */
    private boolean checkBucket(WindowsBucket windowsBucket) {
        if (windowsBucket == null || windowsBucket.success.sum() == 0
                || windowsBucket.rt.sum() == 0) {
            return false;
        }
        return true;
    }

    /**
     * Initializes the minimum response time and maximum number of threads
     */
    private void initMinRtAndMaxThreadNum() {
        systemStatus.setMinRt(Double.MAX_VALUE);
        systemStatus.setMaxThreadNum(Long.MIN_VALUE);
    }

    /**
     * Update the minimum response time and maximum number of threads
     */
    private void updateMinRtAndMaxThreadNum() {
        WindowsBucket windowsBucket = WindowsArray.INSTANCE.getCurrentWindow();

        // total number of successful calls
        long successNum = windowsBucket.success.sum();

        // total response time
        double rt = windowsBucket.rt.sum();

        // number of threads in existence
        double threadNum = windowsBucket.threadNum.sum();
        if (0 != successNum) {
            systemStatus.setMinRt(Math.min(systemStatus.getMinRt(), rt / successNum));
        }
        systemStatus.setMaxThreadNum((long) Math.max(systemStatus.getMaxThreadNum(), threadNum + successNum));
    }

    /**
     * updated qps and average response time
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
