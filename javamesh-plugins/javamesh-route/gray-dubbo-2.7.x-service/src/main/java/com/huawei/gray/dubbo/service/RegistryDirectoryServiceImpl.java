/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.service;

import com.huawei.javamesh.core.util.SpiLoadUtil.SpiWeight;
import com.huawei.gray.dubbo.cache.DubboCache;
import com.huawei.gray.dubbo.utils.RouterUtil;
import com.huawei.route.common.gray.addr.AddrCache;
import com.huawei.route.common.gray.addr.entity.Instances;
import com.huawei.route.common.gray.addr.entity.Metadata;
import com.huawei.route.common.gray.constants.GrayConstant;
import com.huawei.route.common.gray.label.entity.CurrentTag;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.integration.RegistryDirectory;
import org.apache.dubbo.rpc.Invoker;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * RegistryDirectoryInterceptor的service
 *
 * @author pengyuyi
 * @date 2021/11/24
 */
@SpiWeight(5)
public class RegistryDirectoryServiceImpl extends RegistryDirectoryService {
    private static final String URL_INVOKER_FIELD_NAME = "urlInvokerMap";

    @Override
    public void after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        if (obj instanceof RegistryDirectory) {
            // 获取注册监听类
            RegistryDirectory<?> registryDirectory = (RegistryDirectory<?>) obj;
            // 通过反射获取到cachedInvokerUrls变量，其中cachedInvokerUrls为应用的缓存
            Map<String, Invoker<?>> invokerMap = RouterUtil.getField(RegistryDirectory.class, Map.class,
                    registryDirectory, URL_INVOKER_FIELD_NAME);
            if (invokerMap != null) {
                // 将本地路由地址全量更新至本地缓存
                DubboCache.updateLocalAddr(getLocalAddr(invokerMap));
                // 缓存下游实例地址
                AddrCache.setCache(getInstances(invokerMap));
            }
        }
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

    private Map<String, List<Instances>> getInstances(Map<String, Invoker<?>> invokerMap) {
        Map<String, List<Instances>> addrMap = new HashMap<String, List<Instances>>();
        for (Invoker<?> invoker : invokerMap.values()) {
            if (invoker == null || invoker.getUrl() == null) {
                continue;
            }
            URL url = invoker.getUrl();
            String targetService = RouterUtil.getTargetService(url);
            Instances instance = new Instances();
            instance.setIp(url.getIp());
            instance.setLdc(url.getParameter(GrayConstant.GRAY_LDC_KEY, GrayConstant.GRAY_DEFAULT_LDC));
            instance.setPort(url.getPort());
            instance.setServiceName(targetService);
            CurrentTag currentTag = new CurrentTag();
            currentTag.setLdc(url.getParameter(GrayConstant.GRAY_LDC_KEY, GrayConstant.GRAY_DEFAULT_LDC));
            currentTag.setVersion(url.getParameter(GrayConstant.GRAY_VERSION_KEY));
            currentTag.setRegisterVersion(AddrCache.getRegisterVersionCache(url.getAddress()));
            instance.setCurrentTag(currentTag);
            Metadata metadata = new Metadata();
            metadata.setVersion(url.getParameter(GrayConstant.URL_VERSION_KEY));
            metadata.setGroup(url.getParameter(GrayConstant.URL_GROUP_KEY));
            metadata.setClusterName(url.getParameter(GrayConstant.URL_CLUSTER_NAME_KEY));
            instance.setMetadata(metadata);
            if (addrMap.containsKey(targetService)) {
                addrMap.get(targetService).add(instance);
            } else {
                List<Instances> instances = new ArrayList<Instances>();
                instances.add(instance);
                addrMap.put(targetService, instances);
            }
        }
        return addrMap;
    }
}