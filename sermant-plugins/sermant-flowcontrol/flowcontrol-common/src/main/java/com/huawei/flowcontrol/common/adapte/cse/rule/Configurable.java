/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/**
 * Based on org/apache/servicecomb/governance/entity/Configurable.java
 * from the Apache ServiceComb Java Chassis project.
 */

package com.huawei.flowcontrol.common.adapte.cse.rule;

/**
 * 可配置的规则
 * 默认都含有名称与目标服务
 *
 * @author zhouss
 * @since 2021-11-15
 */
public abstract class Configurable {
    /**
     * 配置名
     */
    protected String name;

    /**
     * 目标服务
     */
    protected String services;

    /**
     * 是否合法
     *
     * @return 是否合法
     */
    public abstract boolean isValid();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServices() {
        return services;
    }

    public void setServices(String services) {
        this.services = services;
    }
}
