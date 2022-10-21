/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.discovery.service.retry.policy;

import com.huawei.discovery.entity.ServiceInstance;

/**
 * 重试策略的上下文, 主要记录当前重试的状态。后续会根据重试策略增加
 *
 * @author zhouss
 * @since 2022-10-21
 */
public class PolicyContext {
    /**
     * 当前重试同一个实例的次数
     */
    private int curSameInstanceCount;

    /**
     * 当前正在重试的实例
     */
    private ServiceInstance serviceInstance;

    /**
     * 是否还是使用当前实例进行重试
     *
     * @param maxSameRetry 最大相同的实例重试次数
     * @return true：是
     */
    public boolean isContinue(int maxSameRetry) {
        return ++curSameInstanceCount < maxSameRetry;
    }

    public int getCurSameInstanceCount() {
        return curSameInstanceCount;
    }

    public void setCurSameInstanceCount(int curSameInstanceCount) {
        this.curSameInstanceCount = curSameInstanceCount;
    }

    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }
}
