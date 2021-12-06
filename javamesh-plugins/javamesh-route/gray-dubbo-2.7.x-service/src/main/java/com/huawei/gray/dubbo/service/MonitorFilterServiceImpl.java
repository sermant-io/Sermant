/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.gray.dubbo.service;

import com.huawei.javamesh.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.javamesh.core.util.SpiLoadUtil.SpiWeight;
import com.huawei.gray.dubbo.utils.RouterUtil;
import com.huawei.route.common.gray.constants.GrayConstant;
import com.huawei.route.common.threadlocal.ThreadLocalContext;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.protocol.dubbo.DubboInvoker;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DubboInvokerInterceptor的service
 *
 * @author pengyuyi
 * @date 2021/11/24
 */
@SpiWeight(3)
public class MonitorFilterServiceImpl extends MonitorFilterService {
    private static final Logger LOGGER = LogFactory.getLogger();

    private static final String REQUEST_URL_KEY = "requestUrl";

    @Override
    public void after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        if (isInvalid(arguments)) {
            return;
        }
        // 这里是为了把切走的地址再切回来
        changeInvokerClients(arguments);
        ThreadLocalContext.INSTANCE.remove(REQUEST_URL_KEY);
    }

    /**
     * 拦截点之后执行
     *
     * @param obj 增强的类
     * @param method 增强的方法
     * @param arguments 增强方法的所有参数
     * @param throwable 增强时可能出现的异常
     */
    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
        if (isInvalid(arguments)) {
            return;
        }
        try {
            // 这里是为了把切走的地址再切回来
            changeInvokerClients(arguments);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "MonitorFilterInterceptor is error!", e);
        } catch (NoSuchFieldException e) {
            LOGGER.log(Level.SEVERE, "MonitorFilterInterceptor is error!", e);
        } finally {
            ThreadLocalContext.INSTANCE.remove(REQUEST_URL_KEY);
        }
    }

    private boolean isInvalid(Object[] arguments) {
        Invocation invocation = (Invocation) arguments[1];
        return !(invocation.getInvoker() instanceof DubboInvoker<?>)
                || ThreadLocalContext.INSTANCE.get(REQUEST_URL_KEY) == null;
    }

    private void changeInvokerClients(Object[] arguments) throws NoSuchFieldException, IllegalAccessException {
        Invocation invocation = (Invocation) arguments[1];
        URL requestUrl = (URL) ThreadLocalContext.INSTANCE.get(REQUEST_URL_KEY);
        RouterUtil.changeInvokerClients(invocation, requestUrl.getAddress(), invocation.getInvoker().getUrl(),
                requestUrl.getParameter(GrayConstant.URL_VERSION_KEY),
                requestUrl.getParameter(GrayConstant.URL_GROUP_KEY),
                requestUrl.getParameter(GrayConstant.URL_CLUSTER_NAME_KEY));
    }
}