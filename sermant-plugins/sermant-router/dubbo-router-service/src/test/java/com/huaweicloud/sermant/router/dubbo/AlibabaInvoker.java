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

package com.huaweicloud.sermant.router.dubbo;

import com.huaweicloud.sermant.router.common.constants.RouterConstant;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;

/**
 * 测试类
 *
 * @since 2022-03-18
 */
public class AlibabaInvoker<T> implements Invoker<T> {
    private static final URL ALIBABA_URL = URL.valueOf("dubbo://localhost:8080/com.huaweicloud.foo.BarTest?bar=foo");

    private final URL url;

    /**
     * 构造方法
     *
     * @param version 版本
     */
    public AlibabaInvoker(String version) {
        this.url = ALIBABA_URL.addParameter(RouterConstant.VERSION_KEY, version).setPort(8080);
    }

    @Override
    public Class<T> getInterface() {
        return null;
    }

    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        return null;
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public void destroy() {
    }
}