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

package com.huaweicloud.sermant.router.config.strategy;

import com.huaweicloud.sermant.router.config.entity.Route;

import java.util.List;
import java.util.Map;

/**
 * 路由策略
 *
 * @param <I> 实例泛型
 * @author provenceee
 * @since 2021-10-14
 */
public interface RuleStrategy<I> {
    /**
     * 选取路由的实例
     *
     * @param serviceName 服务名
     * @param instances 实例列表
     * @param routes 路由规则
     * @param isReplaceDash 是否需要替换破折号为点号（dubbo需要）
     * @return 路由过滤后的实例
     */
    List<I> getMatchInstances(String serviceName, List<I> instances, List<Route> routes, boolean isReplaceDash);

    /**
     * 根据请求信息选取路由的实例
     *
     * @param serviceName 服务名
     * @param instances 实例列表
     * @param tags 请求信息
     * @return 路由过滤后的实例
     */
    List<I> getMatchInstancesByRequest(String serviceName, List<I> instances, Map<String, String> tags);

    /**
     * 选取不匹配标签的实例
     *
     * @param serviceName 服务名
     * @param instances 实例列表
     * @param tags 标签
     * @param isReturnAllInstancesWhenMismatch 无匹配时，是否返回全部实例
     * @return 路由过滤后的实例
     */
    List<I> getMismatchInstances(String serviceName, List<I> instances, List<Map<String, String>> tags,
        boolean isReturnAllInstancesWhenMismatch);

    /**
     * 选取同区域的实例
     *
     * @param serviceName 服务名
     * @param instances 实例列表
     * @param zone 区域
     * @return 路由过滤后的实例
     */
    List<I> getZoneInstances(String serviceName, List<I> instances, String zone);
}