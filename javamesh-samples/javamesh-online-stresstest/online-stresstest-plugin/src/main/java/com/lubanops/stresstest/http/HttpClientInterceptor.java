/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.stresstest.http;

import com.huawei.apm.core.agent.common.BeforeResult;
import com.huawei.apm.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.lubanops.stresstest.core.Tester;
import org.apache.http.HttpRequest;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import static com.lubanops.stresstest.config.Constant.TEST_FLAG;
import static com.lubanops.stresstest.config.Constant.TEST_VALUE;

/**
 * HttpClient 拦截器，如果是压测线程发送请求时，则在头部加上压测字段
 *
 * @author yiwei
 * @since 2021/10/21
 */
public class HttpClientInterceptor implements InstanceMethodInterceptor {
    private static final Logger LOGGER = LogFactory.getLogger();

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
        for (Object argument: arguments) {
            if (argument instanceof HttpRequest) {
                LOGGER.fine(String.format("Add stress flag on request:%s", ((HttpRequest) argument).getRequestLine()));
                ((HttpRequest) argument).addHeader(TEST_FLAG, TEST_VALUE);
                break;
            }
        }
    }
}
