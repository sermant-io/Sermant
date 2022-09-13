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

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;

/**
 * 测试类
 *
 * @since 2022-03-18
 */
public class ApacheInvoker<T> implements Invoker<T> {
    private static final URL APACHE_URL = URL
        .valueOf("dubbo://localhost:8080/com.huaweicloud.foo.FooTest?foo=bar&version=0.0.1");

    private final URL url;

    /**
     * 构造方法
     *
     * @param version 版本
     */
    public ApacheInvoker(String version) {
        this.url = APACHE_URL.addParameter(RouterConstant.VERSION_KEY, version).setPort(8080);
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