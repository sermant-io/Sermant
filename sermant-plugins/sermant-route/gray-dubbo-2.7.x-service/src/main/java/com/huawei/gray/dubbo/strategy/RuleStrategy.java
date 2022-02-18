/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.gray.dubbo.strategy;

import com.huawei.route.common.gray.label.entity.Route;
import com.huawei.route.common.gray.label.entity.VersionFrom;

import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;

import java.util.List;

/**
 * 路由策略
 *
 * @author provenceee
 * @since 2021/10/14
 */
public interface RuleStrategy {
    /**
     * 选取灰度应用的invokers
     *
     * @param routes 路由规则
     * @param invocation dubbo invocation
     * @param invokers dubbo invokers
     * @param versionFrom 版本号来源
     * @return 灰度应用的invokers
     */
    List<Invoker<?>> getTargetInvoker(List<Route> routes, Invocation invocation, List<Invoker<?>> invokers,
        VersionFrom versionFrom);
}