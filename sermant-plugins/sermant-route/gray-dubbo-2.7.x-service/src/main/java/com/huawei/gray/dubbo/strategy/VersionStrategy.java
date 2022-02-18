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

import org.apache.dubbo.rpc.Invoker;

/**
 * 版本获取策略
 *
 * @author provenceee
 * @since 2021/12/8
 */
public interface VersionStrategy {
    /**
     * 获取Invoker的版本
     *
     * @param invoker Invoker
     * @return 版本
     */
    String getVersion(Invoker<?> invoker);
}