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

package com.huawei.monitor.interceptor;

import com.huawei.monitor.common.CommonConstant;
import com.huawei.monitor.common.MetricCalEntity;
import com.huawei.monitor.util.MonitorCacheUtil;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.utils.LogUtils;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.support.RpcUtils;

/**
 * DUBBO拦截定义
 *
 * @author zhp
 * @since 2022-11-01
 */
public class AlibabaDubboInterceptor extends AbstractInterceptor {
    private static final String START_TIME = "startTime";

    private static final String MONITOR_NAME = "monitorName";

    private static final String CONNECTOR = ".";

    @Override
    public ExecuteContext before(ExecuteContext context) {
        LogUtils.printDubboRequestBeforePoint(context);
        if (context == null || context.getArguments() == null || context.getArguments().length < 1) {
            return context;
        }
        context.setExtMemberFieldValue(START_TIME, System.currentTimeMillis());
        if (context.getArguments()[0] instanceof Invoker) {
            Invoker<?> invoker = (Invoker<?>) context.getArguments()[0];
            if (!isProvider(invoker)) {
                return context;
            }
            Invocation invocation = (Invocation) context.getArguments()[1];
            String application = invoker.getUrl().getParameter(CommonConstant.DUBBO_APPLICATION);
            String service = invoker.getInterface().getName();
            String method = RpcUtils.getMethodName(invocation);
            String name = application + CONNECTOR + service + CONNECTOR + method;
            context.setExtMemberFieldValue(MONITOR_NAME, name);
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        if (context.getExtMemberFieldValue(MONITOR_NAME) == null) {
            LogUtils.printDubboRequestAfterPoint(context);
            return context;
        }
        String name = (String) context.getExtMemberFieldValue(MONITOR_NAME);
        MetricCalEntity metricCalEntity = MonitorCacheUtil.getMetricCalEntity(name);
        metricCalEntity.getReqNum().incrementAndGet();
        long startTime = (Long) context.getExtMemberFieldValue(START_TIME);
        metricCalEntity.getConsumeReqTimeNum().addAndGet(System.currentTimeMillis() - startTime);
        metricCalEntity.getSuccessFulReqNum().incrementAndGet();
        LogUtils.printDubboRequestAfterPoint(context);
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        if (context.getExtMemberFieldValue(MONITOR_NAME) == null) {
            LogUtils.printDubboRequestOnThrowPoint(context);
            return context;
        }
        String name = (String) context.getExtMemberFieldValue(MONITOR_NAME);
        MetricCalEntity metricCalEntity = MonitorCacheUtil.getMetricCalEntity(name);
        metricCalEntity.getFailedReqNum().incrementAndGet();
        metricCalEntity.getReqNum().incrementAndGet();
        LogUtils.printDubboRequestOnThrowPoint(context);
        return context;
    }

    /**
     * 判断是否为服务提供者
     *
     * @param invoker 远程调用模型
     * @return 是否为服务提供者
     */
    private boolean isProvider(Invoker<?> invoker) {
        return !CommonConstant.DUBBO_CONSUMER.equals(invoker.getUrl().getParameter(CommonConstant.DUBBO_SIDE,
                CommonConstant.DUBBO_PROVIDER));
    }
}
