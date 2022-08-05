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

package com.huaweicloud.loadbalancer.config;

/**
 * 负载均衡上下文
 *
 * @author zhouss
 * @since 2022-08-04
 */
public enum LbContext {
    /**
     * 单例
     */
    INSTANCE;

    /**
     * ribbon负载均衡类型
     */
    public static final String LOADBALANCER_RIBBON = "loadbalancer_ribbon";

    /**
     * spring loadbalancer负载均衡类型
     */
    public static final String LOADBALANCER_SPRING = "loadbalancer_spring";

    /**
     * dubbo负载均衡类型
     */
    public static final String LOADBALANCER_DUBBO = "loadbalancer_dubbo";

    /**
     * 当前服务名
     */
    private String serviceName;

    /**
     * 当前宿主使用的负载均衡类型(ribbon/spring loadbalancer/dubbo)
     */
    private String curLoadbalancerType;

    public String getCurLoadbalancerType() {
        return curLoadbalancerType;
    }

    public void setCurLoadbalancerType(String curLoadbalancerType) {
        this.curLoadbalancerType = curLoadbalancerType;
    }

    /**
     * 是否为目标负载均和类型
     *
     * @param targetLbType 负载均和类型
     * @return true为负载均衡类型
     */
    public boolean isTargetLb(String targetLbType) {
        if (this.curLoadbalancerType == null) {
            return true;
        }
        return this.curLoadbalancerType.equals(targetLbType);
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
