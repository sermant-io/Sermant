/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.config;

/**
 * 离群实力规则
 *
 * @author zhp
 * @since 2023-02-24
 */
public class RemovalRule {
    /**
     * 服务名称或者接口名称
     */
    private String key;

    /**
     * 摘除实例比上限
     */
    private float scaleUpLimit;

    /**
     * 最小实例个数
     */
    private int minInstanceNum;

    /**
     * 失败率阈值
     */
    private float errorRate;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public float getScaleUpLimit() {
        return scaleUpLimit;
    }

    public void setScaleUpLimit(float scaleUpLimit) {
        this.scaleUpLimit = scaleUpLimit;
    }

    public int getMinInstanceNum() {
        return minInstanceNum;
    }

    public void setMinInstanceNum(int minInstanceNum) {
        this.minInstanceNum = minInstanceNum;
    }

    public float getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(float errorRate) {
        this.errorRate = errorRate;
    }
}
