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

package com.huaweicloud.sermant.router.spring.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

/**
 * 测试ControllerInterceptor
 *
 * @author provenceee
 * @since 2022-10-29
 */
public class ControllerInterceptorTest {
    private final ControllerInterceptor interceptor;

    private final ExecuteContext context;

    public ControllerInterceptorTest() {
        interceptor = new ControllerInterceptor();
        context = ExecuteContext.forMemberMethod(new Object(), null, null, null, null);
    }

    /**
     * 重置测试数据
     */
    @Before
    public void clear() {
        ThreadLocalUtils.removeRequestTag();
        ThreadLocalUtils.removeRequestData();
    }

    /**
     * 测试after方法,验证是否释放线程变量
     */
    @Test
    public void testAfter() {
        ThreadLocalUtils.addRequestTag(Collections.emptyMap());

        // 测试after方法,验证是否释放线程变量
        interceptor.after(context);
        Assert.assertNull(ThreadLocalUtils.getRequestTag());
    }

    /**
     * 测试onThrow方法,验证是否释放线程变量
     */
    @Test
    public void testOnThrow() {
        ThreadLocalUtils.addRequestTag(Collections.emptyMap());

        // 测试onThrow方法,验证是否释放线程变量
        interceptor.onThrow(context);
        Assert.assertNull(ThreadLocalUtils.getRequestTag());
    }
}