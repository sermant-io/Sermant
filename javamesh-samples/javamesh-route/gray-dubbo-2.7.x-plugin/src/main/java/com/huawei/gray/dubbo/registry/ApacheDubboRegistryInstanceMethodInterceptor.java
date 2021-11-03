/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.registry;

import com.huawei.apm.bootstrap.common.BeforeResult;
import com.huawei.apm.bootstrap.interceptors.InstanceMethodInterceptor;
import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import com.huawei.gray.dubbo.cache.DubboCache;
import com.huawei.gray.dubbo.utils.RouterUtil;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.integration.RegistryDirectory;
import org.apache.dubbo.rpc.Invoker;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 增强RegistryDirectory类的notify方法，获取并维护应用缓存的路由信息
 *
 * @author l30008180
 * @since 2021年6月28日
 */
public class ApacheDubboRegistryInstanceMethodInterceptor implements InstanceMethodInterceptor {
    private static final Logger LOGGER = LogFactory.getLogger();

    private static final String URL_INVOKER_FIELD_NAME = "urlInvokerMap";

    /**
     * Dubbo每次与注册中心交互之后，同步缓存的URL信息
     *
     * @param obj 增强的类
     * @param method 增强的方法
     * @param arguments 增强方法的所有参数
     * @param beforeResult the method's original return value. May be null if the method triggers an exception.
     * @throws Exception 增强时可能出现的异常
     */
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        RouterUtil.init();
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        if (obj instanceof RegistryDirectory) {
            // 获取注册监听类
            RegistryDirectory<?> registryDirectory = (RegistryDirectory<?>) obj;
            // 通过反射获取到cachedInvokerUrls变量，其中cachedInvokerUrls为应用的缓存
            Map<String, Invoker<?>> invokerMap = RouterUtil.getField(RegistryDirectory.class, Map.class,
                    registryDirectory, URL_INVOKER_FIELD_NAME);
            if (invokerMap != null) {
                // 将本地路由地址全量更新至本地缓存
                DubboCache.updateLocalAddr(getLocalAddr(invokerMap));
                // 缓存地址的版本
                DubboCache.updateLocalParameters(getLocalParameters(invokerMap));
            }
        }
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {
        dealException(t);
    }

    /**
     * 出现异常时，只记录日志
     *
     * @param exception 异常信息
     */
    private void dealException(Throwable exception) {
        LOGGER.log(Level.SEVERE, "Registry error!", exception);
    }

    private Map<String, Map<String, String>> getLocalParameters(Map<String, Invoker<?>> invokerMap) {
        Map<String, Map<String, String>> parameters = new HashMap<String, Map<String, String>>();
        for (Invoker<?> invoker : invokerMap.values()) {
            if (invoker == null || invoker.getUrl() == null) {
                continue;
            }
            URL url = invoker.getUrl();
            parameters.put(url.getAddress(), new HashMap<String, String>(url.getParameters()));
        }
        return parameters;
    }

    private Map<String, Set<String>> getLocalAddr(Map<String, Invoker<?>> invokerMap) {
        Map<String, Set<String>> addrMap = new HashMap<String, Set<String>>();
        for (Invoker<?> invoker : invokerMap.values()) {
            if (invoker == null || invoker.getUrl() == null) {
                continue;
            }
            URL url = invoker.getUrl();
            String targetService = RouterUtil.getTargetService(url);
            if (addrMap.containsKey(targetService)) {
                addrMap.get(targetService).add(url.getAddress());
            } else {
                Set<String> addrs = new HashSet<String>();
                addrs.add(url.getAddress());
                addrMap.put(targetService, addrs);
            }
        }
        return addrMap;
    }
}
