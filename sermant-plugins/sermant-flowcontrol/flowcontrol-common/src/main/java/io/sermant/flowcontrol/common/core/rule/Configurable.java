/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

/**
 * Based on org/apache/servicecomb/governance/entity/Configurable.java
 * from the Apache ServiceComb Java Chassis project.
 */

package io.sermant.flowcontrol.common.core.rule;

/**
 * configurable rules
 * the default contains the name and target service
 *
 * @author zhouss
 * @since 2021-11-15
 */
public abstract class Configurable {
    /**
     * configuration name
     */
    protected String name;

    /**
     * target service
     */
    protected String services;

    /**
     * is invalid
     *
     * @return is invalid
     */
    public abstract boolean isInValid();

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
