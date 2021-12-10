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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.MonitorDataRow;
import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.MultiPrimaryKeyAggregator;
import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.PrimaryKey;
import com.huawei.javamesh.core.lubanops.bootstrap.config.ConfigManager;
import com.huawei.javamesh.core.lubanops.bootstrap.config.Stats;
import com.huawei.javamesh.core.lubanops.bootstrap.sample.SampleConfig;
import com.huawei.javamesh.core.lubanops.bootstrap.sample.SampleType;

public abstract class UrlAggregator extends MultiPrimaryKeyAggregator<UrlStats> {

    /**
     * 慢请求的阈值，比如800毫秒，或者1秒，由用户自己定义,这个定义是通用的，对于单个url也可以自己定义自己的慢请求的阈值
     */
    private int slowRequestThreshold = ConfigManager.getSlowRequestThreshold();

    private Map<String, UrlSlowRequestThreshold> urlSlowRequestThresholds = new HashMap<String, UrlSlowRequestThreshold>();

    /**
     * 全局采样率配置
     */
    private SampleConfig sampleConfig = new SampleConfig();

    /**
     * 慢url
     */
    private List<String> slowUrl = new ArrayList<String>();

    /**
     * 错误url
     */
    private List<String> errorUrl = new ArrayList<String>();

    private AtomicInteger sampleCount = new AtomicInteger(0);

    private AtomicInteger invokeCount = new AtomicInteger(0);

    /**
     * 慢请求的trace个数的阈值
     */
    private Stats slowRequestTraceCountStats = ConfigManager.getSlowRequestTraceCountStats();

    /**
     * 错误请求的trace个数的阈值
     */
    private Stats errorRequestTraceCounStats = ConfigManager.getErrorRequestTraceCounStats();

    /**
     * 正常请求的trace个数的阈值
     */
    private Stats requestTranceCountStats = ConfigManager.getRequestTranceCountStats();

    public List<String> getSlowUrl() {
        return slowUrl;
    }

    public void setSlowUrl(List<String> slowUrl) {
        this.slowUrl = slowUrl;
    }

    public List<String> getErrorUrl() {
        return errorUrl;
    }

    public void setErrorUrl(List<String> errorUrl) {
        this.errorUrl = errorUrl;
    }

    @Override
    public List<MonitorDataRow> harvest() {
        errorRequestTraceCounStats.sampleCount.set(0);
        slowRequestTraceCountStats.sampleCount.set(0);
        requestTranceCountStats.sampleCount.set(0);
        sampleCount.set(0);
        invokeCount.set(0);
        return super.harvest();
    }

    /**
     * 判断是否采样
     * @param url
     * @param method
     * @param sampleConfig
     * @return
     */
    public boolean sample(String url, String method, SampleConfig sampleConfig) {
        invokeCount.addAndGet(1);
        String type = sampleConfig.getSampleType();
        if (type == null) {
            type = SampleType.automatic.value();
        }
        if (SampleType.automatic.value().equals(type)) {
            if (this.getErrorUrl().contains(url)) {
                return resolveSampleStat(url, method, errorRequestTraceCounStats);
            } else if (this.getSlowUrl().contains(url)) {
                return resolveSampleStat(url, method, slowRequestTraceCountStats);
            } else {
                return resolveSampleStat(url, method, requestTranceCountStats);
            }
        } else if (SampleType.all.value().equals(type)) {
            return true;
        } else if (SampleType.frequency.value().equals(type)) {
            return sampleFrequency(url, method, sampleConfig);
        } else if (SampleType.percentage.value().equals(type)) {
            return samplePercentage(url, method, sampleConfig);
        }
        return false;
    }

    /**
     * 判断url根据自动采样规则是否采样
     * @param method
     * @param sampleConfig
     * @param urlStatsAggregator
     * @return
     */
    private boolean resolveSampleStat(String url, String method, Stats stats) {
        PrimaryKey ppk = method == null ? new PrimaryKey(url) : new PrimaryKey(url, method);
        UrlStats urlst = this.obtainValue(ppk);
        if (urlst == null) {
            return false;
        }
        if (stats.sampleCount.addAndGet(1) <= stats.getThreshold()) {
            urlst.getSampleCount().addAndGet(1);
            return true;
        } else if (urlst.getSampleCount().addAndGet(1) <= stats.getMinPerUrl()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 校验每分钟采集数 是否采样
     * @param url
     * @param method
     * @param sampleConfig
     * @return
     */
    private boolean sampleFrequency(String url, String method, SampleConfig sampleConfig) {
        AtomicInteger urlSampleCount;
        PrimaryKey ppk = method == null ? new PrimaryKey(url) : new PrimaryKey(url, method);
        UrlStats urlst = this.obtainValue(ppk);
        if (urlst != null) {
            urlSampleCount = urlst.getSampleCount();
        } else {
            urlSampleCount = this.sampleCount;
        }
        int count = urlSampleCount.addAndGet(1);
        if (count < sampleConfig.getPeriodCount()) {
            return true;
        } else {
            urlSampleCount.decrementAndGet();
            return false;
        }
    }

    /**
     * 校验每分钟采集百分比 是否采样
     * @param url
     * @param method
     * @param sampleConfig
     * @return
     */
    private boolean samplePercentage(String url, String method, SampleConfig sampleConfig) {
        AtomicInteger sampleCount;
        long invokeCount;
        PrimaryKey ppk = method == null ? new PrimaryKey(url) : new PrimaryKey(url, method);
        UrlStats urlst = this.obtainValue(ppk);
        if (urlst != null) {
            sampleCount = urlst.getSampleCount();
            invokeCount = urlst.getInvokeCount();
        } else {
            sampleCount = this.sampleCount;
            invokeCount = this.invokeCount.get();
        }
        int count = sampleCount.addAndGet(1);
        if (invokeCount == 0) {
            if (sampleConfig.getPercentage() > 0) {
                return true;
            } else {
                sampleCount.decrementAndGet();
                return false;
            }
        } else if (count * 100 / invokeCount < sampleConfig.getPercentage()) {
            return true;
        } else {
            sampleCount.decrementAndGet();
            return false;
        }
    }

    public Stats getSlowRequestTraceCountStats() {
        return slowRequestTraceCountStats;
    }

    public void setSlowRequestTraceCountStats(Stats slowRequestTraceCountStats) {
        if (slowRequestTraceCountStats == null) {
            this.slowRequestTraceCountStats = ConfigManager.getSlowRequestTraceCountStats();
        } else {
            this.slowRequestTraceCountStats = slowRequestTraceCountStats;
        }
    }

    public Stats getErrorRequestTraceCounStats() {
        return errorRequestTraceCounStats;
    }

    public void setErrorRequestTraceCounStats(Stats errorRequestTraceCounStats) {
        if (errorRequestTraceCounStats == null) {
            this.errorRequestTraceCounStats = ConfigManager.getErrorRequestTraceCounStats();
        } else {
            this.errorRequestTraceCounStats = errorRequestTraceCounStats;
        }
    }

    public Stats getRequestTranceCountStats() {
        return requestTranceCountStats;
    }

    public void setRequestTranceCountStats(Stats requestTranceCountStats) {
        if (requestTranceCountStats == null) {
            this.requestTranceCountStats = ConfigManager.getRequestTranceCountStats();
        } else {
            this.requestTranceCountStats = requestTranceCountStats;
        }
    }

    public SampleConfig getSampleConfig() {
        return sampleConfig;
    }

    public void setSampleConfig(SampleConfig sampleConfig) {
        this.sampleConfig = sampleConfig;
    }

    public int getSlowRequestThreshold() {
        return slowRequestThreshold;
    }

    public void setSlowRequestThreshold(Integer slowRequestThreshold) {
        if (slowRequestThreshold == null) {
            this.slowRequestThreshold = ConfigManager.getSlowRequestThreshold();
        } else {
            if (slowRequestThreshold > ConfigManager.SLOW_REQUEST_THRESHOLD_H
                    || slowRequestThreshold < ConfigManager.SLOW_REQUEST_THRESHOLD_L) {
                slowRequestThreshold = ConfigManager.DEFAULT_SLOW_REQUEST_THRESHOLD;
            }
            this.slowRequestThreshold = slowRequestThreshold;
        }
    }

    public int getSlowRequestThreshold(String url) {
        UrlSlowRequestThreshold urlSlowRequestThreshold = urlSlowRequestThresholds.get(url);
        if (urlSlowRequestThreshold != null) {
            Integer urlSlowRequestThresholdValue = urlSlowRequestThreshold.getSlowRequestThreshold();
            if (urlSlowRequestThreshold != null) {
                return urlSlowRequestThresholdValue;
            }
        }
        return slowRequestThreshold;
    }

    public Map<String, UrlSlowRequestThreshold> getUrlSlowRequestThresholds() {
        return urlSlowRequestThresholds;
    }

    public void setUrlSlowRequestThresholds(Map<String, UrlSlowRequestThreshold> urlSlowRequestThresholds) {
        this.urlSlowRequestThresholds = urlSlowRequestThresholds;
    }

    public void setUrlSlowRequestThresholds(List<UrlSlowRequestThreshold> urlSlowRequestThresholds) {
        Map<String, UrlSlowRequestThreshold> urlMap = new HashMap<String, UrlSlowRequestThreshold>();
        if (urlSlowRequestThresholds != null) {
            for (int i = 0; i < urlSlowRequestThresholds.size(); i++) {
                UrlSlowRequestThreshold urlSlowRequestThreshold = urlSlowRequestThresholds.get(i);
                urlMap.put(urlSlowRequestThreshold.getUrl(), urlSlowRequestThreshold);
            }
        }
        this.urlSlowRequestThresholds = urlMap;
    }

    public abstract String getUrlKey();

    /**
     * 采集完成后处理 根据用户的配置将慢和错误的url统计出来
     */
    @Override
    public Map<String, List<MonitorDataRow>> afterHarvest(List<MonitorDataRow> collected) {
        if (collected != null) {
            List<String> slowUrl = new ArrayList<String>();
            List<String> errorUrl = new ArrayList<String>();
            for (MonitorDataRow monitorDataRow : collected) {
                String url = (String) monitorDataRow.get(getUrlKey());
                long errorCount = (Long) monitorDataRow.get("errorCount");
                long maxTime = (Long) monitorDataRow.get("maxTime");
                int slowRequestThreshold = getSlowRequestThreshold(url);
                if (errorCount > 0) {
                    errorUrl.add(url);
                } else if (maxTime > slowRequestThreshold) {
                    slowUrl.add(url);
                }
            }
            setSlowUrl(slowUrl);
            setErrorUrl(errorUrl);
        }
        return null;
    }

}
