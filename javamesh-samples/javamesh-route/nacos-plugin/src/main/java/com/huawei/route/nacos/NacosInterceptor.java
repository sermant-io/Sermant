/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.nacos;


import com.alibaba.nacos.api.naming.pojo.Instance;
import com.huawei.apm.core.agent.common.BeforeResult;
import com.huawei.apm.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.route.common.label.heartbeat.HeartbeatInfoProvider;
import com.huawei.route.common.threadlocal.ThreadLocalContext;
import com.huawei.route.common.report.cache.ServiceRegisterCache;
import com.huawei.route.common.report.common.entity.ServiceEssentialMessage;
import com.huawei.route.common.report.common.entity.ServiceRegisterMessage;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * {@link com.alibaba.nacos.client.naming.NacosNamingService} registerInstance拦截
 *
 * @author zhouss
 * @since 2021-10-30
 */
public class NacosInterceptor implements InstanceMethodInterceptor {
    /**
     * 被拦截类中有三个参数时Instance参数的位置
     */
    private static final int INSTANCE_INDEX = 2;

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
        try {
            Instance instance = (Instance) arguments[INSTANCE_INDEX];
            String group = (String) arguments[1];
            Map<String, String> metadata = instance.getMetadata();
            registerHeartbeat(metadata, instance, (String) arguments[0]);
            if (metadata.containsKey("side")) {
                // dubbo应用数据处理
                ServiceRegisterMessage serviceRegisterMessage = new ServiceRegisterMessage();
                serviceRegisterMessage.setServiceName(metadata.get("application"));
                serviceRegisterMessage.setRoot(group);
                serviceRegisterMessage.setClusterName(instance.getClusterName());
                serviceRegisterMessage.setDownServiceName(metadata.get("interface"));
                serviceRegisterMessage.setProtocol("DUBBO");
                serviceRegisterMessage.setRegistry("NACOS");
                serviceRegisterMessage.setPort(instance.getPort());
                serviceRegisterMessage.setIp(instance.getIp());
                serviceRegisterMessage.setNamespaceId(
                        ThreadLocalContext.INSTANCE.get(NacosConstants.NAMESPACE_KEY, String.class));
                ServiceRegisterCache.getInstance().addServiceRegisterMessageByDownService(serviceRegisterMessage);
            } else {
                // spring数据梳理
                ServiceEssentialMessage serviceEssentialMessage = new ServiceEssentialMessage();
                serviceEssentialMessage.setProtocol("SPRING_CLOUD");
                serviceEssentialMessage.setRegistry("NACOS");
                serviceEssentialMessage.setClusterName(instance.getClusterName());
                serviceEssentialMessage.setRoot(group);
                serviceEssentialMessage.setServiceName((String) arguments[0]);
                serviceEssentialMessage.setPort(instance.getPort());
                serviceEssentialMessage.setIp(instance.getIp());
                serviceEssentialMessage.setNamespaceId(
                        ThreadLocalContext.INSTANCE.get(NacosConstants.NAMESPACE_KEY, String.class));
                ServiceRegisterCache.getInstance().addServiceRegisterMessage(serviceEssentialMessage);
            }
        } finally {
            ThreadLocalContext.INSTANCE.remove(NacosConstants.NAMESPACE_KEY);
        }
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) {
        return null;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
    }

    private void registerHeartbeat(Map<String, String> metadata, Instance instance, String serviceName) {
        final HeartbeatInfoProvider heartbeatInfoProvider = HeartbeatInfoProvider.getInstance();
        heartbeatInfoProvider.registerHeartMsg("ip", instance.getIp())
                .registerHeartMsg("registerPort", String.valueOf(instance.getPort()));
        if (metadata.get("side") != null) {
            // dubbo应用使用application作为服务名
            heartbeatInfoProvider.registerHeartMsg("registerServiceName", metadata.get("application"));
        } else {
            heartbeatInfoProvider.registerHeartMsg("registerServiceName", serviceName);
        }
    }
}
