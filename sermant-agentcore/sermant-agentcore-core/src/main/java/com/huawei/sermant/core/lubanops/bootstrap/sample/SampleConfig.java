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

package com.huawei.sermant.core.lubanops.bootstrap.sample;

/**
 * 采样率配置
 * @author
 */
public class SampleConfig {

    private static final int DEFAULT_PERCENTAGE = 10;

    private static final int DEFAULT_PERIOD_COUNT = 1000;

    private String sampleType = SampleType.automatic.value();

    private Integer percentage = DEFAULT_PERCENTAGE;

    private Integer periodCount = DEFAULT_PERIOD_COUNT;

    public String getSampleType() {
        if ((SampleType.frequency.value().equals(sampleType) && periodCount == null)
                && (SampleType.percentage.value().equals(sampleType) && percentage == null)) {
            return null;
        }
        return sampleType;
    }

    public void setSampleType(String sampleType) {
        if (sampleType == null) {
            this.sampleType = SampleType.automatic.value();
        } else {
            this.sampleType = sampleType;
        }
    }

    public Integer getPercentage() {
        return percentage;
    }

    public void setPercentage(String percentage) {
        if (percentage != null) {
            try {
                this.percentage = Integer.valueOf(percentage);
            } catch (Exception e) {
                this.percentage = DEFAULT_PERCENTAGE;
            }
        } else {
            this.percentage = DEFAULT_PERCENTAGE;
        }
    }

    public Integer getPeriodCount() {
        return periodCount;
    }

    public void setPeriodCount(String periodCount) {
        if (periodCount != null) {
            try {
                this.periodCount = Integer.valueOf(periodCount);
            } catch (Exception e) {
                this.periodCount = DEFAULT_PERIOD_COUNT;
            }
        } else {
            this.periodCount = DEFAULT_PERIOD_COUNT;
        }
    }

    public void setPercentage(Integer percentage) {
        this.percentage = percentage;
    }

    public void setPeriodCount(Integer periodCount) {
        this.periodCount = periodCount;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SampleConfig{");
        sb.append("sampleType='").append(sampleType).append('\'');
        sb.append(", percentage=").append(percentage);
        sb.append(", periodCount=").append(periodCount);
        sb.append('}');
        return sb.toString();
    }
}
