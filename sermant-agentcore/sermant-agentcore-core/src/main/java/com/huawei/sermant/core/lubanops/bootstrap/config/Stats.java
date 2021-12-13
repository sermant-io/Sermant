/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.core.lubanops.bootstrap.config;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <br>
 * 对于采样的一些阈值的设置
 * @author
 * @since 2020年3月10日
 */
public class Stats {
    /**
     * 慢请求的默认采集阈值
     */
    public final static Stats SLOW_DEFAULT = new Stats(100, 50, 10, 2);

    /**
     * 错误请求的默认采集阈值
     */
    public final static Stats ERROR_DEFAULT = new Stats(100, 50, 10, 2);

    /**
     * 正常请求的采集阈值
     */
    public final static Stats NORMAL_DEFAULT = new Stats(20, 10, 5, 1);

    /**
     * 低cpu使用场景下的每分钟总采样个数
     */
    public int lowCpuThreshold;

    /**
     * 中等cpu使用场景下的每分钟总采样个数
     */
    public int midCpuThreshold;

    /**
     * 高cpu使用场景下总的cpu每分钟采样个数
     */
    public int highCpuThreshold;

    public AtomicInteger sampleCount = new AtomicInteger(0);

    private static double cpuRatio = 0;

    /**
     * 单个url的最小的采样个数，即使是最高cpu使用的场景
     */
    public int minPerUrl;

    public Stats(int a, int b, int c, int d) {
        this.lowCpuThreshold = a;
        this.midCpuThreshold = b;
        this.highCpuThreshold = c;
        this.minPerUrl = d;
    }

    /**
     * 对服务器端返回的值进行解析，返回的格式是 20,10,5,1 这种格式，多个值以逗号分隔
     * @param s
     * @return
     * @author
     * @since 2020年3月10日
     */
    public static Stats parseValue(String s) {
        String[] ss = s.split(",");
        int a = Integer.parseInt(ss[0]);
        int b = Integer.parseInt(ss[1]);
        int c = Integer.parseInt(ss[2]);
        int d = Integer.parseInt(ss[3]);

        Stats stats = new Stats(a, b, c, d);
        return stats;
    }

    public int getMinPerUrl() {
        return minPerUrl;
    }

    public int getThreshold() {
        if (getCpuRatio() < 30) {
            return lowCpuThreshold;
        } else if (getCpuRatio() < 60) {
            return midCpuThreshold;
        } else {
            return highCpuThreshold;
        }
    }

    public static double getCpuRatio() {
        return cpuRatio;
    }

    public static void setCpuRatio(double cpuRatio) {
        Stats.cpuRatio = cpuRatio;
    }
}
