/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 */

package com.lubanops.stresstest.servlet;

import com.huawei.javamesh.core.agent.common.BeforeResult;
import com.huawei.javamesh.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.javamesh.core.lubanops.bootstrap.utils.StringUtils;
import com.lubanops.stresstest.core.Tester;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

import static com.lubanops.stresstest.config.Constant.TEST_FLAG;

/**
 * HTTP 拦截器，当请求头中包含压测字段时，给线程打上压测标记。
 *
 * @author yiwei
 * @since 2021/10/21
 */
public class WebRequestInterceptor implements InstanceMethodInterceptor {
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
        if (arguments.length > 0 && arguments[0] instanceof HttpServletRequest) {
            parseHttpRequest((HttpServletRequest) arguments[0]);
        }
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) {
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
    }

    private void parseHttpRequest(HttpServletRequest request) {
        String value = request.getHeader(TEST_FLAG);
        if(StringUtils.isBlank(value)) {
            Tester.setTest(false);
            return;
        }
        Tester.setTest(Boolean.parseBoolean(value));
    }
}
