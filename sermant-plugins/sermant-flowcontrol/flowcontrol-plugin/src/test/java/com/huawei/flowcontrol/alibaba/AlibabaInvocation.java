/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol.alibaba;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;

import java.util.HashMap;
import java.util.Map;

/**
 * alibaba 参数类
 *
 * @author zhouss
 * @since 2022-03-03
 */
public class AlibabaInvocation implements Invocation {
    private final Invoker<?> invoker;

    public AlibabaInvocation(Invoker<?> invoker) {
        this.invoker = invoker;
    }

    @Override
    public String getMethodName() {
        return "test";
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return new Class[0];
    }

    @Override
    public Object[] getArguments() {
        return new Object[0];
    }

    @Override
    public Map<String, String> getAttachments() {
        return new HashMap<>();
    }

    @Override
    public String getAttachment(String key) {
        return "";
    }

    @Override
    public String getAttachment(String key, String defaultValue) {
        return "";
    }

    @Override
    public Invoker<?> getInvoker() {
        return invoker;
    }
}
