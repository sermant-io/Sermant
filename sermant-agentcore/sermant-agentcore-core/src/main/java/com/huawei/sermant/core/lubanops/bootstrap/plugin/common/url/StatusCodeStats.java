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

package com.huawei.sermant.core.lubanops.bootstrap.plugin.common.url;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.huawei.sermant.core.lubanops.bootstrap.collector.api.MonitorDataRow;
import com.huawei.sermant.core.lubanops.bootstrap.collector.api.StatsBase;
import com.huawei.sermant.core.lubanops.bootstrap.utils.HarvestUtil;

public class StatusCodeStats implements StatsBase {

    private volatile long countOld;

    private AtomicLong count = new AtomicLong(0);

    private AtomicReference<String> url = new AtomicReference<String>();

    public AtomicLong getCount() {
        return count;
    }

    public void setCount(AtomicLong count) {
        this.count = count;
    }

    public AtomicReference<String> getUrl() {
        return url;
    }

    public void setUrl(AtomicReference<String> url) {
        this.url = url;
    }

    public MonitorDataRow harvest() {
        MonitorDataRow row = new MonitorDataRow();
        countOld = HarvestUtil.getMetricCount(count, countOld, "count", row);
        row.put("url", url.getAndSet(null));
        return row;
    }

    @Override
    public MonitorDataRow getStatus() {
        MonitorDataRow row = new MonitorDataRow();
        row.put("count", count.get());
        row.put("url", url.get());
        return row;
    }

}
