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

package com.huawei.sermant.stresstest.http;

import static com.huawei.sermant.stresstest.config.Constant.TEST_FLAG;
import static com.huawei.sermant.stresstest.config.Constant.TEST_VALUE;

import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.stresstest.core.Tester;

import org.apache.http.HttpRequest;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * HttpClient 拦截器，如果是压测线程发送请求时，则在头部加上压测字段
 *
 * @author yiwei
 * @since 2021-10-21
 */
public class HttpClientInterceptor implements InstanceMethodInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
        if (Tester.isTest()) {
            handleHttpRequest(arguments);
        }
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) {
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
    }

    private void handleHttpRequest(Object[] arguments) {
        for (Object argument : arguments) {
            if (argument instanceof HttpRequest) {
                LOGGER.fine(String.format(Locale.ROOT, "Add stress flag on request:%s",
                    ((HttpRequest)argument).getRequestLine()));
                ((HttpRequest)argument).addHeader(TEST_FLAG, TEST_VALUE);
                break;
            }
        }
    }
}
