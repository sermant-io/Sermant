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

package com.huawei.javamesh.core.lubanops.bootstrap.plugin.common.url;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.MonitorDataRow;
import com.huawei.javamesh.core.lubanops.bootstrap.plugin.common.DefaultSectionStats;

public class UrlStats extends DefaultSectionStats {

    public volatile boolean has200 = false;

    private AtomicReference<String> maxTimeUsedUrl = new AtomicReference<String>();// httpRequest中原生的url没有经过agent映射的

    private AtomicInteger sampleCount = new AtomicInteger(0);

    public long onFinally(long start, String nativeUrl, boolean isError) {
        long endTime = System.nanoTime();
        long useTime = endTime - start;
        boolean isMaxTime = super.onFinally(useTime);
        if (isMaxTime) {
            maxTimeUsedUrl.set(nativeUrl);
        }
        if (isError) {
            errorCountIncrement();
        }
        return useTime;
    }

    private void errorCountIncrement() {
        this.errorCount.incrementAndGet();
    }

    public String harvestMaxTimeNativeUrl() {
        return maxTimeUsedUrl.getAndSet(null);
    }

    public void runningCountDecrement() {
        runningCount.decrementAndGet();
    }

    @Override
    public MonitorDataRow harvest(int[] newRanges) {
        sampleCount.set(0);
        return super.harvest(newRanges);
    }

    public AtomicInteger getSampleCount() {
        return sampleCount;
    }

    public long getInvokeCount() {
        return invokeCount.get() - invokeCountOld;
    }

}
