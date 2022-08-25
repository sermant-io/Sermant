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

package com.huawei.example.demo.interceptor;

import com.huawei.example.demo.service.DemoNameService;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 拦截器单元测试示例模版
 *
 * @author lilai
 * @version 1.0.0
 * @since 2022-08-25
 */
public class DemoInterceptorTest {
    /**
     * 被测试的拦截器
     */
    private DemoMemberInterceptor interceptor;

    /**
     * 被增强的对象
     */
    private Object object;

    /**
     * 被增强的方法
     */
    private Method method;

    /**
     * 被增强的方法入参
     */
    private Object[] arguments;

    /**
     * 额外的静态属性
     */
    private Map<String, Object> extStaticFields;

    /**
     * 额外的成员属性
     */
    private Map<String, Object> extMemberFields;

    /**
     * 测试类构造方法，初始化相关参数
     */
    public DemoInterceptorTest() throws NoSuchMethodException {
        this.interceptor = new DemoMemberInterceptor();
        this.object = new Object();
        this.method = DemoNameService.class.getMethod("configFunc");
        this.arguments = new Object[1];
        this.arguments[0] = "test";
        this.extStaticFields = new HashMap<>();
        this.extMemberFields = new HashMap<>();
    }

    /**
     * 构建插件执行上下文，以成员方法为例
     */
    private ExecuteContext buildContext(Object object, Method method, Object[] arguments,
                                        Map<String, Object> extStaticFields, Map<String, Object> extMemberFields) {
        return ExecuteContext.forMemberMethod(object, method, arguments, extStaticFields, extMemberFields);
    }

    /**
     * 测试拦截器before方法
     */
    @Test
    public void testBefore() throws Exception {
        ExecuteContext context = buildContext(object, method, arguments, extStaticFields, extMemberFields);
        interceptor.before(context);

        // 根据拦截器before方法实现的功能来验证增强是否生效，此仅处为示例
        Assert.assertEquals("test", context.getArguments()[0]);
    }

    /**
     * 测试拦截器after方法
     */
    @Test
    public void testAfter() throws Exception {
        ExecuteContext context = buildContext(object, method, arguments, null, null);
        interceptor.after(context);

        // 根据拦截器after方法实现的功能来验证增强是否生效，此处仅为示例
        Assert.assertEquals("test", context.getArguments()[0]);
    }

    /**
     * 测试拦截器onThrow方法
     */
    @Test
    public void testOnThrow() throws Exception {
        ExecuteContext context = buildContext(object, method, arguments, null, null);
        interceptor.onThrow(context);

        // 根据拦截器onThrow方法实现的功能来验证增强是否生效，此处为仅示例
        Assert.assertEquals("test", context.getArguments()[0]);
    }
}