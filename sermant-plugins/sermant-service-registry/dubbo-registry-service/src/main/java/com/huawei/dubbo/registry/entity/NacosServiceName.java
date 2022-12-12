/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.dubbo.registry.entity;

import com.huawei.dubbo.registry.utils.ReflectUtils;
import com.huawei.registry.config.NacosRegisterConfig;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.StringUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * nacos服务名管理
 *
 * @since 2022-10-25
 */
public class NacosServiceName {
    private static final String NAME_SEPARATOR = ":";

    private static final String VALUE_SEPARATOR = ",";

    private static final String WILDCARD = "*";

    private static final String CATEGORY_KEY = "category";

    private static final String INTERFACE_KEY = "interface";

    private static final int CATEGORY_INDEX = 0;

    private static final int SERVICE_INTERFACE_INDEX = 1;

    private static final int SERVICE_GROUP_INDEX = 2;

    private static final String DEFAULT_CATEGORY = "providers";

    private final String category;

    private final String serviceInterface;

    private final String group;

    private String value;

    /**
     * url构造类
     *
     * @param url url
     */
    public NacosServiceName(Object url) {
        serviceInterface = ReflectUtils.getParameter(url, INTERFACE_KEY);
        category = isValid(serviceInterface) ? DEFAULT_CATEGORY : ReflectUtils.getParameter(url, CATEGORY_KEY);
        NacosRegisterConfig nacosRegisterConfig =
                PluginConfigManager.getPluginConfig(NacosRegisterConfig.class);
        group = nacosRegisterConfig.getGroup();
        value = toValue();
    }

    /**
     * 服务名构造类
     *
     * @param value serviceName
     */
    public NacosServiceName(String value) {
        this.value = value;
        String[] segments = value.split(NAME_SEPARATOR, -1);
        this.category = segments[CATEGORY_INDEX];
        this.serviceInterface = segments[SERVICE_INTERFACE_INDEX];
        this.group = segments[SERVICE_GROUP_INDEX];
    }

    /**
     * 判断接口、版本、分组是否含“，”、“*”号
     *
     * @return 是否包含
     */
    public boolean isValid() {
        return isValid(serviceInterface) && isValid(group);
    }

    private boolean isValid(String val) {
        return !isWildcard(val) && !isRange(val);
    }

    /**
     * 判断服务名称是否相同、包含“,”
     *
     * @param concreteServiceName 服务名
     * @return boolean
     */
    public boolean isCompatible(NacosServiceName concreteServiceName) {
        if (!concreteServiceName.isValid()) {
            return false;
        }
        if (!StringUtils.equals(this.category, concreteServiceName.category)
                && !matchRange(this.category, concreteServiceName.category)) {
            return false;
        }
        if (!StringUtils.equals(this.serviceInterface, concreteServiceName.serviceInterface)) {
            return false;
        }
        if (isWildcard(this.group)) {
            return true;
        }
        return StringUtils.equals(this.group, concreteServiceName.group)
                || matchRange(this.group, concreteServiceName.group);
    }

    private boolean matchRange(String range, String val) {
        if (StringUtils.isBlank(range)) {
            return true;
        }
        if (!isRange(range)) {
            return false;
        }
        String[] values = range.split(VALUE_SEPARATOR);
        return Arrays.asList(values).contains(val);
    }

    private boolean isWildcard(String val) {
        return WILDCARD.equals(val);
    }

    private boolean isRange(String val) {
        return val != null && val.contains(VALUE_SEPARATOR) && val.split(VALUE_SEPARATOR).length > 1;
    }

    /**
     * 获取value
     *
     * @return value
     */
    public String getValue() {
        if (value == null) {
            value = toValue();
        }
        return value;
    }

    private String toValue() {
        return category
                + NAME_SEPARATOR + serviceInterface
                + NAME_SEPARATOR + group;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NacosServiceName)) {
            return false;
        }
        NacosServiceName that = (NacosServiceName) obj;
        return Objects.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }

    @Override
    public String toString() {
        return getValue();
    }
}
