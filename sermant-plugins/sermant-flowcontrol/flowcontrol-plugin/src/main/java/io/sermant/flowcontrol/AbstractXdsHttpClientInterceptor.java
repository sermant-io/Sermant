/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.flowcontrol;

import io.github.resilience4j.retry.Retry;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.core.service.xds.entity.XdsInstanceCircuitBreakers;
import io.sermant.core.service.xds.entity.XdsRequestCircuitBreakers;
import io.sermant.core.service.xds.entity.XdsRetryPolicy;
import io.sermant.core.utils.CollectionUtils;
import io.sermant.flowcontrol.common.config.CommonConst;
import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.common.entity.RequestEntity;
import io.sermant.flowcontrol.common.exception.InvokerWrapperException;
import io.sermant.flowcontrol.common.handler.retry.RetryContext;
import io.sermant.flowcontrol.common.handler.retry.policy.RetryPolicy;
import io.sermant.flowcontrol.common.util.StringUtils;
import io.sermant.flowcontrol.common.util.XdsThreadLocalUtil;
import io.sermant.flowcontrol.common.xds.circuit.XdsCircuitBreakerManager;
import io.sermant.flowcontrol.common.xds.handler.XdsHandler;
import io.sermant.flowcontrol.common.xds.lb.XdsLoadBalancer;
import io.sermant.flowcontrol.common.xds.lb.XdsLoadBalancerFactory;
import io.sermant.flowcontrol.service.InterceptorSupporter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Enhance the client request sending functionality by performing Xds service instance discovery and circuit breaking
 * during the request sending process
 *
 * @author zhp
 * @since 2024-11-30
 */
public abstract class AbstractXdsHttpClientInterceptor extends InterceptorSupporter {
    protected static final String MESSAGE = "CircuitBreaker has forced open and deny any requests";

    protected static final Logger LOGGER = LoggerFactory.getLogger();

    private static final int MIN_SUCCESS_CODE = 200;

    private static final int MAX_SUCCESS_CODE = 399;

    private static final int HUNDRED = 100;

    protected final io.sermant.flowcontrol.common.handler.retry.Retry retry;

    /**
     * Constructor
     *
     * @param retry Retry instance, used for retry determination
     */
    public AbstractXdsHttpClientInterceptor(io.sermant.flowcontrol.common.handler.retry.Retry retry) {
        this.retry = retry;
    }

    /**
     * Perform circuit breaker judgment and handling
     *
     * @return The result of whether circuit breaking is needed
     */
    public boolean isNeedCircuitBreak() {
        FlowControlScenario scenarioInfo = XdsThreadLocalUtil.getScenarioInfo();
        if (scenarioInfo == null || StringUtils.isEmpty(scenarioInfo.getServiceName())
                || StringUtils.isEmpty(scenarioInfo.getClusterName())) {
            return false;
        }
        Optional<XdsRequestCircuitBreakers> circuitBreakersOptional = XdsHandler.INSTANCE.
                getRequestCircuitBreakers(scenarioInfo.getServiceName(), scenarioInfo.getClusterName());
        if (!circuitBreakersOptional.isPresent()) {
            return false;
        }
        int activeRequestNum = XdsCircuitBreakerManager.incrementActiveRequests(scenarioInfo.getServiceName(),
                scenarioInfo.getClusterName());
        int maxRequest = circuitBreakersOptional.get().getMaxRequests();
        return maxRequest > 0 && activeRequestNum > maxRequest;
    }

    /**
     * Execute method invocation and retry logic
     *
     * @param context The execution context of the Interceptor
     */
    public void executeWithRetryPolicy(ExecuteContext context) {
        Object result = context.getResult();
        Throwable ex = context.getThrowable();

        // Create logical function for service invocation or retry
        final Supplier<Object> retryFunc = createRetryFunc(context);
        RetryContext.INSTANCE.markRetry(retry);
        try {
            // first execution taking over the host logic
            result = retryFunc.get();
            context.changeResult(result);
        } catch (Throwable throwable) {
            ex = throwable;
            log(throwable);
        }
        context.afterMethod(result, ex);
        try {
            final List<Retry> handlers = getRetryHandlers();

            // Determine whether retry is necessary
            if (!handlers.isEmpty() && isNeedRetry(handlers.get(0), result, ex)) {
                // execute retry logic
                result = handlers.get(0).executeCheckedSupplier(retryFunc::get);
                context.skip(result);
                return;
            }
            if (ex == null) {
                context.skip(result);
                return;
            }
            context.setThrowableOut(getRealCause(ex));
        } catch (Throwable throwable) {
            LOGGER.warning(String.format(Locale.ENGLISH,
                    "Failed to invoke method:%s for few times, reason:%s",
                    context.getMethod().getName(), getExMsg(throwable)));
            context.setThrowableOut(getRealCause(throwable));
        } finally {
            RetryContext.INSTANCE.remove();
        }
        if (context.getThrowableOut() != null) {
            onThrow(context);
        }
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        FlowControlScenario scenarioInfo = XdsThreadLocalUtil.getScenarioInfo();
        Object requestEntity = context.getLocalFieldValue(CommonConst.REQUEST_INFO);
        if (requestEntity instanceof RequestEntity) {
            getXdsHttpFlowControlService().onAfter((RequestEntity) requestEntity, context.getResult(), scenarioInfo);
        }
        if (context.getThrowableOut() == null) {
            decreaseActiveRequestsAndCountFailureRequests(context, scenarioInfo);
        }
        XdsThreadLocalUtil.removeSendByteFlag();
        XdsThreadLocalUtil.removeScenarioInfo();
        return context;
    }

    @Override
    public ExecuteContext doThrow(ExecuteContext context) {
        XdsThreadLocalUtil.removeSendByteFlag();
        FlowControlScenario scenarioInfo = XdsThreadLocalUtil.getScenarioInfo();
        Object requestEntity = context.getLocalFieldValue(CommonConst.REQUEST_INFO);
        if (requestEntity instanceof RequestEntity) {
            getXdsHttpFlowControlService().onThrow((RequestEntity) requestEntity, context.getThrowable(), scenarioInfo);
        }
        if (context.getThrowableOut() != null) {
            decreaseActiveRequestsAndCountFailureRequests(context, scenarioInfo);
            XdsThreadLocalUtil.removeSendByteFlag();
            XdsThreadLocalUtil.removeScenarioInfo();
        }
        return context;
    }

    private void decreaseActiveRequestsAndCountFailureRequests(ExecuteContext context,
                                                               FlowControlScenario scenarioInfo) {
        if (scenarioInfo == null || StringUtils.isEmpty(scenarioInfo.getServiceName())
                || StringUtils.isEmpty(scenarioInfo.getClusterName())) {
            return;
        }
        XdsCircuitBreakerManager.decreaseActiveRequests(scenarioInfo.getServiceName(), scenarioInfo.getClusterName());
        int statusCode = getStatusCode(context);
        if (statusCode >= MIN_SUCCESS_CODE && statusCode <= MAX_SUCCESS_CODE) {
            return;
        }
        handleFailedRequests(scenarioInfo, statusCode);
    }

    /**
     * handle failure request
     *
     * @param statusCode response code
     * @param scenario scenario information
     */
    private void handleFailedRequests(FlowControlScenario scenario, int statusCode) {
        Optional<XdsInstanceCircuitBreakers> instanceCircuitBreakersOptional = XdsHandler.INSTANCE.
                getInstanceCircuitBreakers(scenario.getServiceName(), scenario.getClusterName());
        if (!instanceCircuitBreakersOptional.isPresent()) {
            return;
        }
        XdsInstanceCircuitBreakers circuitBreakers = instanceCircuitBreakersOptional.get();
        XdsCircuitBreakerManager.recordFailureRequest(scenario, scenario.getAddress(), statusCode, circuitBreakers);
        XdsCircuitBreakerManager.setCircuitBeakerStatus(circuitBreakers, scenario);
    }

    /**
     * Get status code
     *
     * @param context The execution context of the Interceptor
     * @return response code
     */
    protected abstract int getStatusCode(ExecuteContext context);

    /**
     * choose serviceInstance by xds rule
     *
     * @return result
     */
    protected Optional<ServiceInstance> chooseServiceInstanceForXds() {
        FlowControlScenario scenarioInfo = XdsThreadLocalUtil.getScenarioInfo();
        if (scenarioInfo == null || StringUtils.isEmpty(scenarioInfo.getServiceName())) {
            return Optional.empty();
        }
        if (StringUtils.isEmpty(scenarioInfo.getClusterName())) {
            scenarioInfo.setClusterName(StringUtils.EMPTY);
            Set<ServiceInstance> serviceInstanceSet = XdsHandler.INSTANCE.
                    getServiceInstanceByServiceName(scenarioInfo.getServiceName());
            if (CollectionUtils.isEmpty(serviceInstanceSet)) {
                return Optional.empty();
            }
            return Optional.ofNullable(chooseServiceInstanceByLoadBalancer(serviceInstanceSet, scenarioInfo));
        }
        Set<ServiceInstance> serviceInstanceSet = XdsHandler.INSTANCE.
                getMatchedServiceInstance(scenarioInfo.getServiceName(), scenarioInfo.getClusterName());
        if (CollectionUtils.isEmpty(serviceInstanceSet)) {
            return Optional.empty();
        }
        if (RetryContext.INSTANCE.isPolicyNeedRetry()) {
            removeRetriedServiceInstance(serviceInstanceSet);
        }
        removeCircuitBreakerInstance(scenarioInfo, serviceInstanceSet);
        return Optional.ofNullable(chooseServiceInstanceByLoadBalancer(serviceInstanceSet, scenarioInfo));
    }

    private void removeRetriedServiceInstance(Set<ServiceInstance> serviceInstanceSet) {
        RetryPolicy retryPolicy = RetryContext.INSTANCE.getRetryPolicy();
        retryPolicy.retryMark();
        Set<Object> retriedInstance = retryPolicy.getAllRetriedInstance();
        Set<ServiceInstance> allInstance = new HashSet<>(serviceInstanceSet);
        for (Object retryInstance : retriedInstance) {
            if (retryInstance instanceof ServiceInstance) {
                serviceInstanceSet.remove(retryInstance);
            }
        }
        if (CollectionUtils.isEmpty(serviceInstanceSet)) {
            serviceInstanceSet.addAll(allInstance);
        }
    }

    private ServiceInstance chooseServiceInstanceByLoadBalancer(Set<ServiceInstance> instanceSet,
            FlowControlScenario scenarioInfo) {
        XdsLoadBalancer loadBalancer = XdsLoadBalancerFactory.getLoadBalancer(scenarioInfo.getServiceName(),
                scenarioInfo.getClusterName());
        return loadBalancer.selectInstance(new ArrayList<>(instanceSet));
    }

    private void removeCircuitBreakerInstance(FlowControlScenario scenarioInfo, Set<ServiceInstance> instanceSet) {
        Optional<XdsInstanceCircuitBreakers> instanceCircuitBreakersOptional = XdsHandler.INSTANCE.
                getInstanceCircuitBreakers(scenarioInfo.getServiceName(), scenarioInfo.getClusterName());
        if (!instanceCircuitBreakersOptional.isPresent()) {
            return;
        }
        XdsInstanceCircuitBreakers outlierDetection = instanceCircuitBreakersOptional.get();
        int count = instanceSet.size();
        if (checkMinInstanceNum(outlierDetection, count)) {
            return;
        }
        List<ServiceInstance> circuitBreakerInstances = new ArrayList<>();
        float maxCircuitBreakerPercent = (float) outlierDetection.getMaxEjectionPercent() / HUNDRED;
        int maxCircuitBreakerInstances = (int) Math.floor(count * maxCircuitBreakerPercent);
        for (ServiceInstance serviceInstance : instanceSet) {
            if (maxCircuitBreakerPercent > 0
                    && hasReachedCircuitBreakerThreshold(circuitBreakerInstances, maxCircuitBreakerInstances)) {
                break;
            }
            String address = serviceInstance.getHost() + CommonConst.CONNECT + serviceInstance.getPort();
            if (XdsCircuitBreakerManager.needsInstanceCircuitBreaker(scenarioInfo, address)) {
                circuitBreakerInstances.add(serviceInstance);
            }
        }
        if (checkHealthInstanceNum(count, outlierDetection, circuitBreakerInstances.size())) {
            return;
        }
        circuitBreakerInstances.forEach(instanceSet::remove);
    }

    private boolean hasReachedCircuitBreakerThreshold(List<ServiceInstance> circuitBreakerInstances,
                                                      int maxCircuitBreakerInstances) {
        return circuitBreakerInstances.size() >= maxCircuitBreakerInstances;
    }

    private boolean checkHealthInstanceNum(int count, XdsInstanceCircuitBreakers outlierDetection, int size) {
        return count * outlierDetection.getMinHealthPercent() >= (count - size);
    }

    private boolean checkMinInstanceNum(XdsInstanceCircuitBreakers outlierDetection, int count) {
        return outlierDetection.getFailurePercentageMinimumHosts() > count;
    }

    /**
     * Get Retry Handler
     *
     * @return Retry Handlers
     */
    protected List<Retry> getRetryHandlers() {
        FlowControlScenario scenarioInfo = XdsThreadLocalUtil.getScenarioInfo();
        if (scenarioInfo == null || StringUtils.isEmpty(scenarioInfo.getServiceName())
                || StringUtils.isEmpty(scenarioInfo.getRouteName())) {
            return Collections.emptyList();
        }
        Optional<XdsRetryPolicy> retryPolicyOptional = XdsHandler.INSTANCE
                .getRetryPolicy(scenarioInfo.getServiceName(), scenarioInfo.getRouteName());
        if (!retryPolicyOptional.isPresent()) {
            return Collections.emptyList();
        }
        XdsRetryPolicy retryPolicy = retryPolicyOptional.get();
        RetryContext.INSTANCE.buildXdsRetryPolicy(retryPolicy);
        return getRetryHandler().getXdsRetryHandlers(scenarioInfo, retryPolicy);
    }

    /**
     * create retry method
     *
     * @param context The execution context of the Interceptor
     * @return Define Supplier for retry calls of service calls
     * @throws InvokerWrapperException InvokerWrapperException
     */
    protected Supplier<Object> createRetryFunc(ExecuteContext context) {
        Object obj = context.getObject();
        Method method = context.getMethod();
        Object[] allArguments = context.getArguments();
        final AtomicBoolean isFirstInvoke = new AtomicBoolean(true);
        return () -> {
            method.setAccessible(true);
            try {
                preRetry(obj, method, allArguments, context.getResult(), isFirstInvoke.get());
                isFirstInvoke.compareAndSet(true, false);
                Object invokeResult = method.invoke(obj, allArguments);
                context.changeResult(invokeResult);
                return invokeResult;
            } catch (IllegalAccessException ignored) {
                isFirstInvoke.compareAndSet(true, false);
            } catch (InvocationTargetException ex) {
                isFirstInvoke.compareAndSet(true, false);
                throw new InvokerWrapperException(ex.getTargetException());
            }
            return context.getResult();
        };
    }

    /**
     * Pre-processing for retry calls
     *
     * @param obj enhancement class
     * @param method target method
     * @param allArguments method parameter
     * @param result default result
     * @param isFirstInvoke Is this the first invocation
     */
    protected abstract void preRetry(Object obj, Method method, Object[] allArguments, Object result,
            boolean isFirstInvoke);
}
