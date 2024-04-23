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

package io.sermant.discovery.service.retry.policy;

import io.sermant.discovery.entity.ServiceInstance;

/**
 * The context of the retry policy, which primarily records the state of the current retries. The number of follow-ups
 * will be increased based on the retry policy
 *
 * @author zhouss
 * @since 2022-10-21
 */
public class PolicyContext {
    /**
     * The current number of retries for the same instance
     */
    private int curSameInstanceCount;

    /**
     * The instance that is currently being retried
     */
    private ServiceInstance serviceInstance;

    /**
     * Whether to retry with the current instance
     *
     * @param maxSameRetry The maximum number of retries for the same instance
     * @return true：yes
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
