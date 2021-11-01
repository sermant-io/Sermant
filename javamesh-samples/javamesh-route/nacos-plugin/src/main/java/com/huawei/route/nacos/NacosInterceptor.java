/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.nacos;


import com.alibaba.nacos.api.naming.pojo.Instance;
import com.huawei.apm.bootstrap.common.BeforeResult;
import com.huawei.apm.bootstrap.interceptors.InstanceMethodInterceptor;
import com.huawei.route.nacos.heartbeat.HeartbeatInfoProvider;

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
        Instance instance = (Instance) arguments[INSTANCE_INDEX];
        String group = (String) arguments[1];
        Map<String, String> metadata = instance.getMetadata();
        registerHeartbeat(metadata, instance, (String)arguments[0]);
        /*if ("consumer".equals(metadata.get("side")) || metadata.containsValue("SPRING_CLOUD")) {
            // instance.getClusterName() == null is dubbo else spring cloud
            if (instance.getClusterName() == null) {
                ServiceRegisterMessage serviceRegisterMessage = new ServiceRegisterMessage();
                serviceRegisterMessage.setServiceName(metadata.get("application"));
                serviceRegisterMessage.setRoot(group);
                serviceRegisterMessage.setClusterName(instance.getClusterName());
                serviceRegisterMessage.setDownServiceName(metadata.get("interface"));
                serviceRegisterMessage.setProtocol("DUBBO");
                serviceRegisterMessage.setRegistry("NACOS");
                ServiceRegisterCache.getInstance().addServiceRegisterMessageByDownService(serviceRegisterMessage);
            } else {
                ServiceEssentialMessage serviceEssentialMessage = new ServiceEssentialMessage();
                serviceEssentialMessage.setProtocol("SPRING_CLOUD");
                serviceEssentialMessage.setRegistry("NACOS");
                serviceEssentialMessage.setClusterName(instance.getClusterName());
                serviceEssentialMessage.setRoot(group);
                serviceEssentialMessage.setServiceName((String) allArguments[0]);
                ServiceRegisterCache.getInstance().addServiceRegisterMessage(serviceEssentialMessage);
            }
        }*/
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) {
        return null;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {

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
