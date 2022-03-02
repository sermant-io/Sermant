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

package com.huawei.flowcontrol.retry;

import com.huawei.flowcontrol.common.entity.DubboRequestEntity;
import com.huawei.flowcontrol.common.handler.retry.AbstractRetry;
import com.huawei.flowcontrol.common.handler.retry.Retry;
import com.huawei.flowcontrol.common.handler.retry.RetryContext;
import com.huawei.flowcontrol.common.util.ConvertUtils;
import com.huawei.flowcontrol.service.InterceptorSupporter;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.cluster.LoadBalance;
import com.alibaba.dubbo.rpc.cluster.support.AbstractClusterInvoker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

/**
 * alibaba dubbo拦截后的增强类,埋点定义sentinel资源
 *
 * @author zhouss
 * @since 2022-02-10
 */
public class AlibabaDubboInvokerInterceptor extends InterceptorSupporter {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final Retry retry = new AlibabaDubboRetry();

    /**
     * 转换apache dubbo 注意，该方法不可抽出，由于宿主依赖仅可由该拦截器加载，因此抽出会导致找不到类
     *
     * @param invocation 调用信息
     * @return DubboRequestEntity
     */
    private DubboRequestEntity convertToAlibabaDubboEntity(Invocation invocation) {
        // 高版本使用api invocation.getTargetServiceUniqueName获取路径，此处使用版本加接口，达到的最终结果一致
        String apiPath = ConvertUtils.buildApiPath(invocation.getInvoker().getInterface().getName(),
            invocation.getAttachment(ConvertUtils.DUBBO_ATTACHMENT_VERSION), invocation.getMethodName());
        return new DubboRequestEntity(apiPath, invocation.getAttachments());
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private Object invokeRetryMethod(Object obj, Object[] allArguments, Object ret, boolean isNeedThrow,
        boolean isRetry) throws Throwable {
        try {
            if (obj instanceof AbstractClusterInvoker) {
                final Invocation invocation = (Invocation) allArguments[0];
                final List<Invoker<?>> invokers = (List<Invoker<?>>) allArguments[1];
                final Method checkInvokers = getMethodCheckInvokers();
                final Method select = getMethodSelect();

                if (checkInvokers == null || select == null) {
                    LOGGER.warning(String.format(Locale.ENGLISH, "It does not support retry for class %s",
                        obj.getClass().getCanonicalName()));
                    return ret;
                }
                if (isRetry) {
                    invocation.getAttachments().put(RETRY_KEY, RETRY_VALUE);
                }

                // 校验invokers
                checkInvokers.invoke(obj, invokers, invocation);
                LoadBalance loadBalance = (LoadBalance) allArguments[2];

                // 选择invoker
                final Invoker<?> invoke = (Invoker<?>) select.invoke(obj, loadBalance, invocation, invokers, null);

                // 执行调用
                final Result result = invoke.invoke(invocation);
                if (result.hasException() && isNeedThrow) {
                    throw result.getException();
                }
                return result;
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
            LOGGER.warning("No such Method ! " + ex.getMessage());
        }
        return ret;
    }

    private Method getMethodSelect() {
        return getInvokerMethod("select", func -> {
            try {
                final Method method = AbstractClusterInvoker.class
                    .getDeclaredMethod("select", LoadBalance.class, Invocation.class, List.class, List.class);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ex) {
                LOGGER.warning("No such Method! " + ex.getMessage());
            }
            return null;
        });
    }

    private Method getMethodCheckInvokers() {
        return getInvokerMethod("checkInvokers", func -> {
            try {
                final Method method = AbstractClusterInvoker.class
                    .getDeclaredMethod("checkInvokers", List.class, Invocation.class);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ex) {
                LOGGER.warning("No such Method! " + ex.getMessage());
            }
            return null;
        });
    }

    @Override
    protected final ExecuteContext doBefore(ExecuteContext context) {
        context.skip(null);
        return context;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Override
    protected final ExecuteContext doAfter(ExecuteContext context) {
        final Object[] allArguments = context.getArguments();
        final Invocation invocation = (Invocation) allArguments[0];
        Object result = context.getResult();
        try {
            // 标记当前线程执行重试
            RetryContext.INSTANCE.markRetry(retry);
            result = invokeRetryMethod(context.getObject(), allArguments, result, false, false);
            final List<io.github.resilience4j.retry.Retry> handlers = retryHandler
                .getHandlers(convertToAlibabaDubboEntity(invocation));
            if (!handlers.isEmpty() && needRetry(handlers.get(0), result, ((RpcResult) result).getException())) {
                result = handlers.get(0).executeCheckedSupplier(
                    () -> invokeRetryMethod(context.getObject(), allArguments, context.getResult(), true,
                        true));
            }
        } catch (Throwable throwable) {
            result = new RpcResult(throwable);
        } finally {
            RetryContext.INSTANCE.removeRetry();
        }
        context.changeResult(result);
        return context;
    }

    public static class AlibabaDubboRetry extends AbstractRetry {
        @Override
        public boolean needRetry(Set<String> statusList, Object result) {
            // dubbo不支持状态码
            return false;
        }

        @Override
        public Class<? extends Throwable>[] retryExceptions() {
            return getRetryExceptions();
        }

        @Override
        public RetryFramework retryType() {
            return RetryFramework.ALIBABA_DUBBO;
        }
    }
}
