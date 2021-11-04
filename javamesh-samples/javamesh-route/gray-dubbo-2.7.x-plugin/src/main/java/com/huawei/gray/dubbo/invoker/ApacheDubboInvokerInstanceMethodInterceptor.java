/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.invoker;

import com.huawei.apm.core.agent.common.BeforeResult;
import com.huawei.apm.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.gray.dubbo.cache.DubboCache;
import com.huawei.gray.dubbo.strategy.RuleStrategyEnum;
import com.huawei.gray.dubbo.utils.RouterUtil;
import com.huawei.route.common.gray.addr.AddrCache;
import com.huawei.route.common.gray.addr.entity.Instances;
import com.huawei.route.common.gray.addr.entity.Metadata;
import com.huawei.route.common.gray.constants.GrayConstant;
import com.huawei.route.common.gray.label.LabelCache;
import com.huawei.route.common.gray.label.entity.GrayConfiguration;
import com.huawei.route.common.gray.label.entity.Route;
import com.huawei.route.common.gray.label.entity.Rule;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.remoting.exchange.ExchangeClient;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.protocol.dubbo.DubboInvoker;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 增强DubboInvoker类的doInvoke方法，更改路由信息
 *
 * @author l30008180
 * @since 2021年6月28日
 */
public class ApacheDubboInvokerInstanceMethodInterceptor implements InstanceMethodInterceptor {
    private static final Logger LOGGER = LogFactory.getLogger();

    private static final String INVOKER_FIELD_CLIENTS = "clients";

    private static final String CLIENT_FIELD_URL = "url";

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        Invocation invocation = (Invocation) arguments[0];
        URL requestUrl = invocation.getInvoker().getUrl();
        String targetService = RouterUtil.getTargetService(requestUrl);
        GrayConfiguration grayConfiguration = LabelCache.getLabel(DubboCache.getAppName());
        String targetVersion = null;
        String group = null;
        String clusterName = null;
        if (GrayConfiguration.isInValid(grayConfiguration)) {
            Set<String> localAddrList = DubboCache.getLocalAddrList(targetService);
            // 这个是为了切换应用后，再把标签失效了，能切回来
            if (!localAddrList.contains(requestUrl.getAddress())) {
                String targetServiceIp = localAddrList.toArray(new String[0])[0];
                Map<String, String> localParameters = DubboCache.getLocalParameters(targetServiceIp);
                if (!CollectionUtils.isEmpty(localParameters)) {
                    targetVersion = localParameters.get(GrayConstant.URL_VERSION_KEY);
                    group = localParameters.get(GrayConstant.URL_GROUP_KEY);
                    clusterName = localParameters.get(GrayConstant.URL_CLUSTER_NAME_KEY);
                }
                changeInvokerClients((DubboInvoker<?>) invocation.getInvoker(), targetServiceIp, requestUrl,
                        targetVersion, group, clusterName);
            }
            return;
        }
        if (RpcContext.getContext().isConsumerSide()) {
            String version = grayConfiguration.getCurrentTag().getVersion();
            String interfaceName = requestUrl.getServiceInterface() + "." + invocation.getMethodName();
            List<Rule> rules = RouterUtil.getValidRules(grayConfiguration, targetService, interfaceName);
            List<Route> routes = RouterUtil.getRoutes(rules, invocation.getArguments());
            RuleStrategyEnum ruleStrategyEnum =
                    CollectionUtils.isEmpty(routes) ? RuleStrategyEnum.UPSTREAM : RuleStrategyEnum.WEIGHT;
            String targetServiceIp = ruleStrategyEnum.getTargetServiceIp(routes, targetService, interfaceName, version,
                    invocation);
            Instances instance = AddrCache.getInstance(targetService, targetServiceIp);
            if (instance != null && instance.getMetadata() != null) {
                Metadata metadata = instance.getMetadata();
                targetVersion = metadata.getVersion();
                group = metadata.getGroup();
                clusterName = metadata.getClusterName();
            }
            changeInvokerClients((DubboInvoker<?>) invocation.getInvoker(), targetServiceIp, requestUrl, targetVersion,
                    group, clusterName);
        }
    }

    /**
     * 每次进行Dubbo请求时，更换请求的IP地址
     *
     * @param obj 增强的类
     * @param method 增强的方法
     * @param arguments 增强方法的所有参数
     * @param result the method's original return value. May be null if the method triggers an exception.
     * @return 返回值
     * @throws Exception 增强时可能出现的异常
     */
    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        Result rpcResult = (Result) result;
        if (rpcResult != null && rpcResult.getException() != null) {
            dealException(rpcResult.getException());
        }
        return rpcResult;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
        dealException(throwable);
    }

    /**
     * 出现异常时，只记录日志
     *
     * @param throwable 异常信息
     */
    private void dealException(Throwable throwable) {
        LOGGER.log(Level.SEVERE, "Registry error!", throwable);
    }

    private void changeInvokerClients(DubboInvoker<?> invoker, String host, URL requestUrl, String version,
            String group, String clusterName) throws NoSuchFieldException, IllegalAccessException {
        if (host == null || host.length() == 0 || invoker == null || requestUrl == null) {
            return;
        }
        // 反射获取clients变量
        ExchangeClient[] clients = (ExchangeClient[]) RouterUtil.getField(invoker, INVOKER_FIELD_CLIENTS);
        List<ExchangeClient> clientList = new ArrayList<ExchangeClient>();
        // 获取请求的接口、方法等属性
        String serviceName = RouterUtil.getServiceName(requestUrl);
        String interfaceName = RouterUtil.getInterfaceName(requestUrl);
        for (ExchangeClient client : clients) {
            URL clientUrl;
            try { // 因为类可见性问题，只能用catch异常的方法解决
                clientUrl = (URL) RouterUtil.getField(client, CLIENT_FIELD_URL);
            } catch (NoSuchFieldException e) {
                // 如果没有URL属性，则跳过
                continue;
            } catch (IllegalAccessException e) {
                // 如果没有URL属性，则跳过
                continue;
            }
            // 找到请求接口相同的client，放入列表中
            if (serviceName == null || !serviceName.equals(RouterUtil.getServiceName(clientUrl))) {
                continue;
            }
            if (interfaceName == null || !interfaceName.equals(RouterUtil.getInterfaceName(clientUrl))) {
                continue;
            }
            clientList.add(client);
        }
        // 更换client中的地址
        List<ExchangeClient> buildClients = new ArrayList<ExchangeClient>();
        for (ExchangeClient exchangeClient : clientList) {
            // 更换client中的URL地址
            boolean isChanged = RouterUtil.rebuildUrlByIpAndPort(host, exchangeClient, version, group, clusterName);
            if (isChanged) { // 更换失败则不作为将要使用的地址
                buildClients.add(exchangeClient);
            }
            RouterUtil.rebuildUrlByIpAndPort(host, invoker, version, group, clusterName);
        }
        RouterUtil.setField(invoker, INVOKER_FIELD_CLIENTS, buildClients.toArray(new ExchangeClient[0]));
    }
}
