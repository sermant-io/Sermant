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

package com.huawei.discovery.service.lb.discovery;

/**
 * 实例监听变更, 此功能将与{@link com.huawei.discovery.service.lb.cache.InstanceCacheManager 绑定}
 *
 * @author zhouss
 * @since 2022-10-12
 */
public interface InstanceListenable {
    /**
     * 初始化
     */
    void init();

    /**
     * 尝试增加指定服务实例监听, 若已监听则直接返回
     *
     * @param serviceName 指定服务名
     * @param listener 监听器
     */
    void tryAdd(String serviceName, InstanceChangeListener listener);

    /**
     * 关闭监听器
     */
    void close();

    /**
     * 该监听器名称
     *
     * @return 名称
     */
    String name();
}
