/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.flowreplay.invocation;

import com.huawei.flowre.flowreplay.config.Const;
import com.huawei.flowre.flowreplay.domain.content.DubboInvokeContent;
import com.huawei.flowre.flowreplay.domain.result.DubboRequestResult;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.service.GenericService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * dubbo类应用回放service 泛化调用
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-03-04
 */
@Component
public class DubboReplayInvocationImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(DubboReplayInvocationImpl.class);

    @Autowired
    Environment environment;

    @Value("${zookeeper.connect.timeout}")
    private int timeOut;

    public DubboRequestResult invoke(DubboInvokeContent dubboInvokeContent) {
        try {
            InetAddress address = InetAddress.getLocalHost();
            String workerName = Const.WORKER_NAME_PREFIX + address.getHostAddress()
                + Const.UNDERLINE + environment.getProperty(Const.SERVER_PORT);
            ApplicationConfig applicationConfig = new ApplicationConfig();
            applicationConfig.setName(workerName);
            RegistryConfig registryConfig = new RegistryConfig();
            registryConfig.setAddress(dubboInvokeContent.getAddress());
            applicationConfig.setRegistry(registryConfig);
            ReferenceConfig<GenericService> reference = new ReferenceConfig<GenericService>();
            reference.setApplication(applicationConfig);
            reference.setTimeout(timeOut);

            // 声明为泛化调用接口
            reference.setGeneric(true);

            // 配置泛化调用接口信息
            reference.setInterface(dubboInvokeContent.getInterfaceName());
            if (!Const.INVALID_VERSION.equals(dubboInvokeContent.getVersion())) {
                reference.setVersion(dubboInvokeContent.getVersion());
            }
            reference.setGroup(dubboInvokeContent.getGroup());

            // 隐式传参
            RpcContext rpcContext = RpcContext.getContext();
            rpcContext.setAttachment(Const.TRACE_ID, dubboInvokeContent.getAttachments().get(Const.TRACE_ID));
            rpcContext.setAttachment(Const.RECORD_JOB_ID, dubboInvokeContent.getAttachments().get(Const.RECORD_JOB_ID));
            DubboRequestResult dubboRequestResult = new DubboRequestResult();
            try {
                GenericService genericService = reference.get();
                Date start = new Date();
                Object result = genericService.$invoke(dubboInvokeContent.getMethodName(),
                    dubboInvokeContent.getParametersTypeList(), dubboInvokeContent.getParametersList());
                Date end = new Date();
                dubboRequestResult.setResult(result);
                dubboRequestResult.setResponseTime(end.getTime() - start.getTime());
            } catch (IllegalStateException illegalStateException) {
                LOGGER.error("Service not found : {}", illegalStateException.getMessage());
            } catch (Exception exception) {
                LOGGER.error("Dubbo invoke error : {}", exception.getMessage());
            }
            reference.destroy();
            return dubboRequestResult;
        } catch (UnknownHostException unknownHostException) {
            LOGGER.error("UnknownHost : {}", unknownHostException.getMessage());
        }
        return new DubboRequestResult();
    }
}
