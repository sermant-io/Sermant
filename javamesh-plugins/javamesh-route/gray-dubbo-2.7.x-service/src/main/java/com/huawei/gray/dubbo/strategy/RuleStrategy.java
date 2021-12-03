/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

import org.apache.dubbo.rpc.Invocation;

import java.util.List;

/**
 * 路由策略
 *
 * @author pengyuyi
 * @date 2021/10/14
 */
public interface RuleStrategy {
    /**
     * 获取目标地址ip
     *
     * @param list 路由规则
     * @param targetService 目标服务
     * @param interfaceName 接口
     * @param version 当前服务的版本
     * @param invocation dubbo invocation
     * @return 目标地址 ip:port
     */
    String getTargetServiceIp(List<Route> list, String targetService, String interfaceName, String version,
            Invocation invocation);
}
