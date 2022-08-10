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

package com.huaweicloud.loadbalancer.rule;

/**
 * 负载均衡
 *
 * @author zhouss
 * @since 2022-08-09
 */
public class LoadbalancerRule {
    private String serviceName;

    /**
     * 负载均衡类型
     */
    private String loadbalancerType;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getLoadbalancerType() {
        return loadbalancerType;
    }

    public void setLoadbalancerType(String loadbalancerType) {
        this.loadbalancerType = loadbalancerType;
    }

    @Override
    public String toString() {
        return "LoadbalancerRule{"
                + "serviceName='" + serviceName + '\''
                + ", loadbalancerType='" + loadbalancerType + '\'' + '}';
    }
}
