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

package com.huawei.sermant.stresstest.servlet;

import static com.huawei.sermant.stresstest.config.Constant.TEST_FLAG;

import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.sermant.core.utils.StringUtils;
import com.huawei.sermant.stresstest.core.Tester;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

/**
 * HTTP 拦截器，当请求头中包含压测字段时，给线程打上压测标记。
 *
 * @author yiwei
 * @since 2021-10-21
 */
public class WebRequestInterceptor implements InstanceMethodInterceptor {
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
        if (arguments.length > 0 && arguments[0] instanceof HttpServletRequest) {
            parseHttpRequest((HttpServletRequest)arguments[0]);
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
        if (StringUtils.isBlank(value)) {
            Tester.setTest(false);
            return;
        }
        Tester.setTest(Boolean.parseBoolean(value));
    }
}
