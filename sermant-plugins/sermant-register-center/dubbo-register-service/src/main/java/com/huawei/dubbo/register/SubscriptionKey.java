/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.dubbo.register;

import java.util.Objects;

/**
 * 订阅数据的key
 *
 * @author provenceee
 * @since 2021/12/23
 */
public class SubscriptionKey {
    private final String appId;
    private final String serviceName;
    private final String interfaceName;

    /**
     * 构造方法
     *
     * @param appId appId
     * @param serviceName 服务名
     * @param interfaceName 接口名
     */
    public SubscriptionKey(String appId, String serviceName, String interfaceName) {
        this.appId = appId;
        this.serviceName = serviceName;
        this.interfaceName = interfaceName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass()) {
            SubscriptionKey that = (SubscriptionKey) obj;
            return Objects.equals(appId, that.appId) && Objects.equals(serviceName, that.serviceName)
                && Objects.equals(interfaceName, that.interfaceName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(appId, serviceName, interfaceName);
    }
}
