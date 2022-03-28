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

package com.huawei.example.demo.interceptor;

import com.huawei.example.demo.common.DemoLogger;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huawei.sermant.core.plugin.agent.interceptor.AbstractInterceptor;

/**
 * 用于测试局部变量、成员变量和全局变量的写入和读取
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-22
 */
public class DemoFieldSetInterceptor extends AbstractInterceptor {
    private static final String LOCALFIELDNAME = "testLocal";
    private static final String MEMEBERFIELDNAME = "testMember";
    private static final String STATICFIELDNAME = "testStatic";

    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        final Object localField = context.getLocalFieldValue(LOCALFIELDNAME);
        final int newLocalField = localField == null ? 1 : Integer.parseInt(localField.toString()) + 1;
        context.setLocalFieldValue(LOCALFIELDNAME, newLocalField);
        final Object memberField = context.getMemberFieldValue(MEMEBERFIELDNAME);
        final int newMemberField = memberField == null ? 1 : Integer.parseInt(memberField.toString()) + 1;
        context.setMemberFieldValue(MEMEBERFIELDNAME, newMemberField);
        final Object staticField = context.getStaticFieldValue(STATICFIELDNAME);
        final int newStaticField = staticField == null ? 1 : Integer.parseInt(staticField.toString()) + 1;
        context.setStaticFieldValue(STATICFIELDNAME, newStaticField);
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        DemoLogger.println("[DemoFieldSetInterceptor]-testLocal: " + context.getLocalFieldValue(LOCALFIELDNAME));
        DemoLogger.println("[DemoFieldSetInterceptor]-testMember: " + context.getMemberFieldValue(MEMEBERFIELDNAME));
        DemoLogger.println("[DemoFieldSetInterceptor]-testStatic: " + context.getStaticFieldValue(STATICFIELDNAME));
        return context;
    }
}
