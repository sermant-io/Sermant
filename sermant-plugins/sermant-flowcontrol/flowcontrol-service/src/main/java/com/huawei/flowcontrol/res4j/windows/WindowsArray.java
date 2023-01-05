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

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.stream.Stream;

/**
 * 滑动窗口
 *
 * @author xuezechao1
 * @since 2022-12-07
 */
public enum WindowsArray {

    /**
     * 单例
     */
    INSTANCE;

    /**
     * 默认窗口大小
     */
    private static final int DEFAULT_WINDOWS_SIZE = 60;

    /**
     * 滑动窗口数组
     */
    private static AtomicReferenceArray<WindowsBucket> windowsArray = null;

    /**
     * 初始化
     */
    public void initWindowsArray() {
        windowsArray = new AtomicReferenceArray<>(DEFAULT_WINDOWS_SIZE);
        Stream.iterate(0, n -> n + 1).limit(DEFAULT_WINDOWS_SIZE)
                .forEach(a -> windowsArray.set(a, new WindowsBucket()));
    }

    /**
     * 获取当前时间点窗口
     *
     * @return 当前时间点窗口
     */
    public WindowsBucket getCurrentWindow() {
        return windowsArray.get(calculateCurrentWindowsIndex());
    }

    /**
     * 获取当前时间点前一个窗口
     *
     * @return 当前时间点前一个窗口
     */
    public WindowsBucket getPreviousWindow() {
        return windowsArray.get(calculatePreviousWindowsIndex());
    }

    /**
     * 获取指定索引窗口
     *
     * @param index 索引
     * @return 指定索引窗口
     */
    public WindowsBucket getWindow(int index) {
        return windowsArray.get(index);
    }

    /**
     * 获取当前线程数
     *
     * @return 当前线程数
     */
    public long getThreadNum() {
        return getCurrentWindow().threadNum.sum();
    }

    /**
     * 增加成功数
     *
     * @param startTime 请求时间
     */
    public void addSuccess(long startTime) {
        windowsArray.get(calculateWindowsIndex(startTime)).success.increment();
    }

    /**
     * 增加响应时间
     *
     * @param startTime 请求时间
     * @param responseTime 响应时间
     */
    public void addRt(long startTime, long responseTime) {
        windowsArray.get(calculateWindowsIndex(startTime)).rt.add(responseTime);
    }

    /**
     * 增加线程数
     *
     * @param startTime 请求时间
     */
    public void addThreadNum(long startTime) {
        windowsArray.get(calculateWindowsIndex(startTime)).threadNum.increment();
    }

    /**
     * 减少线程数
     *
     * @param startTime 请求时间
     */
    public void decreaseThreadNum(long startTime) {
        windowsArray.get(calculateWindowsIndex(startTime)).threadNum.decrement();
    }

    /**
     * 计算当前时间点窗口索引
     *
     * @return 当前时间点窗口索引
     */
    public int calculateCurrentWindowsIndex() {
        return Calendar.getInstance().get(Calendar.SECOND) % DEFAULT_WINDOWS_SIZE;
    }

    /**
     * 重置下一个窗口数据
     */
    public void resetNextWindows() {
        WindowsBucket windowsBucket = getWindow(calculateNextWindowsIndex());
        windowsBucket.success.reset();
        windowsBucket.rt.reset();
        windowsBucket.threadNum.reset();
    }

    /**
     * 计算当前时间点下一个窗口索引
     *
     * @return 当前时间点下一个窗口索引
     */
    public int calculateNextWindowsIndex() {
        int nextIndex = calculateCurrentWindowsIndex() + 1;
        if (nextIndex >= DEFAULT_WINDOWS_SIZE) {
            nextIndex = 0;
        }
        return nextIndex;
    }

    /**
     * 计算窗口索引
     *
     * @param startTime 请求时间
     * @return 窗口索引
     */
    public int calculateWindowsIndex(long startTime) {
        if (System.currentTimeMillis() - startTime > DEFAULT_WINDOWS_SIZE * CommonConst.S_MS_UNIT) {
            return -1;
        }
        return (int) (startTime / CommonConst.S_MS_UNIT % DEFAULT_WINDOWS_SIZE);
    }

    /**
     * 计算当前时间前一个窗口索引
     *
     * @return 当前时间前一个窗口索引
     */
    public int calculatePreviousWindowsIndex() {
        int previousIndex = calculateCurrentWindowsIndex() - 1;
        if (previousIndex < 0) {
            previousIndex = DEFAULT_WINDOWS_SIZE - 1;
        }
        return previousIndex;
    }

    /**
     * 获取滑动窗口数组
     *
     * @return 滑动窗口
     */
    public AtomicReferenceArray<WindowsBucket> getWindowsArray() {
        return windowsArray;
    }
}
