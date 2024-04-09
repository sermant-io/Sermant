/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.flowcontrol.apache;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.AsyncRpcResult;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.service.GenericService;

/**
 * apache invoker
 *
 * @author zhouss
 * @since 2022-03-03
 */
public class ApacheInvoker implements Invoker<GenericService> {
    @Override
    public Class<GenericService> getInterface() {
        return GenericService.class;
    }

    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        return AsyncRpcResult.newDefaultAsyncResult(new ApacheInvocation(new ApacheInvoker()));
    }

    @Override
    public URL getUrl() {
        return URL.valueOf("localhost:8080");
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void destroy() {

    }
}
