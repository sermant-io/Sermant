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

package com.huawei.sermant.core.lubanops.bootstrap.plugin.common;

import com.huawei.sermant.core.lubanops.bootstrap.collector.api.MonitorDataRow;
import com.huawei.sermant.core.lubanops.bootstrap.collector.api.StatsBase;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultStatusCodeStats implements StatsBase {
    // 记录上一次读取的值，用于获取差值
    private volatile long countOld;

    // 存储总数
    private AtomicLong count = new AtomicLong(0);

    private AtomicReference<String> url = new AtomicReference<String>();

    public AtomicLong getCount() {
        return count;
    }

    public AtomicReference<String> getUrl() {
        return url;
    }

    @Override
    public MonitorDataRow getStatus() {
        MonitorDataRow row = new MonitorDataRow();
        row.put("count", count.get());
        row.put("url", url.get());
        return row;
    }

    @Override
    public MonitorDataRow harvest() {
        long countNew = count.get();
        long countDelta;
        if ((countDelta = countNew - countOld) > 0) {
            MonitorDataRow row = new MonitorDataRow();
            row.put("count", countDelta);
            row.put("url", url.getAndSet(null));
            // reset
            countOld = countNew;
            return row;
        }
        return null;
    }
}
