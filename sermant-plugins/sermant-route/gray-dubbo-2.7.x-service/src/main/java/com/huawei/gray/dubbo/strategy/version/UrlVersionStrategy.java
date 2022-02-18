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

package com.huawei.gray.dubbo.strategy.version;

import com.huawei.gray.dubbo.strategy.VersionStrategy;
import com.huawei.route.common.gray.constants.GrayConstant;

import org.apache.dubbo.rpc.Invoker;

/**
 * 从注册url中获取版本
 *
 * @author provenceee
 * @since 2021/12/8
 */
public class UrlVersionStrategy implements VersionStrategy {
    /**
     * 从注册url中获取版本
     *
     * @param invoker Invoker
     * @return 版本
     */
    @Override
    public String getVersion(Invoker<?> invoker) {
        return invoker.getUrl().getParameter(GrayConstant.GRAY_VERSION_KEY, GrayConstant.GRAY_DEFAULT_VERSION);
    }
}