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
 * sliding window
 *
 * @author xuezechao1
 * @since 2022-12-07
 */
public enum WindowsArray {
    /**
     * single case
     */
    INSTANCE;

    /**
     * default window size
     */
    private static final int DEFAULT_WINDOWS_SIZE = 60;

    /**
     * sliding window array
     */
    private static AtomicReferenceArray<WindowsBucket> windowsArray = null;

    /**
     * initialize
     */
    public void initWindowsArray() {
        windowsArray = new AtomicReferenceArray<>(DEFAULT_WINDOWS_SIZE);
        Stream.iterate(0, num -> num + 1).limit(DEFAULT_WINDOWS_SIZE)
                .forEach(index -> windowsArray.set(index, new WindowsBucket()));
    }

    /**
     * gets the current point in time window
     *
     * @return current point in time window
     */
    public WindowsBucket getCurrentWindow() {
        return windowsArray.get(calculateCurrentWindowsIndex());
    }

    /**
     * gets a window before the current point in time
     *
     * @return window before the current point in time
     */
    public WindowsBucket getPreviousWindow() {
        return windowsArray.get(calculatePreviousWindowsIndex());
    }

    /**
     * gets the window for the specified index
     *
     * @param index index
     * @return the window for the specified index
     */
    public WindowsBucket getWindow(int index) {
        return windowsArray.get(index);
    }

    /**
     * gets the current number of threads
     *
     * @return the current number of threads
     */
    public long getThreadNum() {
        return getCurrentWindow().threadNum.sum();
    }

    /**
     * increase success count
     *
     * @param startTime requestTime
     */
    public void addSuccess(long startTime) {
        windowsArray.get(calculateWindowsIndex(startTime)).success.increment();
    }

    /**
     * increase response time
     *
     * @param startTime requestTime
     * @param responseTime responseTime
     */
    public void addRt(long startTime, long responseTime) {
        windowsArray.get(calculateWindowsIndex(startTime)).rt.add(responseTime);
    }

    /**
     * increase thread count
     *
     * @param startTime requestTime
     */
    public void addThreadNum(long startTime) {
        windowsArray.get(calculateWindowsIndex(startTime)).threadNum.increment();
    }

    /**
     * reduce thread count
     *
     * @param startTime requestTime
     */
    public void decreaseThreadNum(long startTime) {
        windowsArray.get(calculateWindowsIndex(startTime)).threadNum.decrement();
    }

    /**
     * Computes the window index for the current point in time
     *
     * @return the window index for the current point in time
     */
    public int calculateCurrentWindowsIndex() {
        return Calendar.getInstance().get(Calendar.SECOND) % DEFAULT_WINDOWS_SIZE;
    }

    /**
     * reset the next window data
     */
    public void resetNextWindows() {
        WindowsBucket windowsBucket = getWindow(calculateNextWindowsIndex());
        windowsBucket.success.reset();
        windowsBucket.rt.reset();
        windowsBucket.threadNum.reset();
    }

    /**
     * Calculates the index of the next window at the current point in time
     *
     * @return the index of the next window at the current point in time
     */
    public int calculateNextWindowsIndex() {
        int nextIndex = calculateCurrentWindowsIndex() + 1;
        if (nextIndex >= DEFAULT_WINDOWS_SIZE) {
            nextIndex = 0;
        }
        return nextIndex;
    }

    /**
     * compute window index
     *
     * @param startTime request time
     * @return window index
     */
    public int calculateWindowsIndex(long startTime) {
        if (System.currentTimeMillis() - startTime > DEFAULT_WINDOWS_SIZE * CommonConst.S_MS_UNIT) {
            return -1;
        }
        return (int) (startTime / CommonConst.S_MS_UNIT % DEFAULT_WINDOWS_SIZE);
    }

    /**
     * Calculates the index of the previous window at the current time
     *
     * @return the index of the previous window at the current time
     */
    public int calculatePreviousWindowsIndex() {
        int previousIndex = calculateCurrentWindowsIndex() - 1;
        if (previousIndex < 0) {
            previousIndex = DEFAULT_WINDOWS_SIZE - 1;
        }
        return previousIndex;
    }

    /**
     * gets the sliding window array
     *
     * @return the sliding window array
     */
    public AtomicReferenceArray<WindowsBucket> getWindowsArray() {
        return windowsArray;
    }
}
