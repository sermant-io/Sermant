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

package com.huawei.discovery.entity;

import java.util.Map;

/**
 * 实例
 *
 * @author zhouss
 * @since 2022-09-26
 */
public interface ServiceInstance {
    /**
     * 唯一标识符 ip:port
     *
     * @return id
     */
    String getId();

    /**
     * 所属服务名
     *
     * @return 服务名
     */
    String getServiceName();

    /**
     * 获取域名
     *
     * @return 域名
     */
    String getHost();

    /**
     * 获取IP地址
     *
     * @return IP
     */
    String getIp();

    /**
     * 端口
     *
     * @return port
     */
    int getPort();

    /**
     * 获取源数据
     *
     * @return metadata
     */
    Map<String, String> getMetadata();

    /**
     * 状态
     *
     * @return 服务状态
     */
    String status();

    /**
     * 判断是否与目标相等
     *
     * @param target 目标对象
     * @return 是否相等
     */
    @Override
    boolean equals(Object target);

    /**
     * 重写hashcode方法
     *
     * @return hash码
     */
    @Override
    int hashCode();

    /**
     * 服务实例状态
     *
     * @since 2022-09-28
     */
    enum Status {
        /**
         * 可用
         */
        UP,

        /**
         * 不可用
         */
        DOWN,

        /**
         * 未知
         */
        UN_KNOW;
    }
}
