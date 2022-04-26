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

package com.huawei.dubbo.registry.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.Objects;

/**
 * dubbo接口的key
 *
 * @author provenceee
 * @since 2022-04-06
 */
public class InterfaceKey {
    @JSONField(serialzeFeatures = SerializerFeature.WriteMapNullValue)
    private final String group;

    @JSONField(serialzeFeatures = SerializerFeature.WriteMapNullValue)
    private final String version;

    /**
     * 构造方法
     *
     * @param group 组
     * @param version 版本
     */
    public InterfaceKey(String group, String version) {
        this.group = group;
        this.version = version;
    }

    public String getGroup() {
        return group;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass()) {
            InterfaceKey that = (InterfaceKey) obj;
            return Objects.equals(group, that.group) && Objects.equals(version, that.version);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, version);
    }
}