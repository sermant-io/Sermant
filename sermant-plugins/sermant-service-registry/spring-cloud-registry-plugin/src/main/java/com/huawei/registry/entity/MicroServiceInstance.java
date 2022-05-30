/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.registry.entity;

import java.util.Map;

/**
 * 定义实例信息
 *
 * @author zhouss
 * @since 2022-02-17
 */
public interface MicroServiceInstance {
    /**
     * 服务名
     *
     * @return 服务名
     */
    String getServiceName();

    /**
     * 域名
     *
     * @return host
     */
    String getHost();

    /**
     * 当前实例的IP地址
     *
     * @return ip
     */
    String getIp();

    /**
     * port
     *
     * @return 端口
     */
    int getPort();

    /**
     * 服务ID
     *
     * @return 服务ID
     */
    String getServiceId();

    /**
     * 实例ID
     *
     * @return 实例ID
     */
    String getInstanceId();

    /**
     * 获取实例的元数据信息
     *
     * @return 元数据
     */
    Map<String, String> getMetadata();
}
