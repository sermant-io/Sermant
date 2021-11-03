/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.report;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import com.huawei.route.report.common.entity.ServiceRegisterMessage;
import com.huawei.route.report.send.ServiceRegistrarMessageSender;
import com.huawei.route.report.acquire.TargetAddrAcquire;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

/**
 * 调服务注册信息用接口发送数据
 *
 * @author zhengbin zhao
 * @version 1.0
 * @since 2021-07-15
 */
public class SendServiceRegisterMessage implements ServiceRegistrarMessageSender {
    private static final Logger LOGGER = LogFactory.getLogger();

    @Override
    public Set<ServiceRegisterMessage> sendServiceRegisterMessage(Set<ServiceRegisterMessage> serviceRegisterMessages)
        throws IOException {
        String serviceRegisterRequestJson = JSON.toJSONString(serviceRegisterMessages, SerializerFeature.DisableCircularReferenceDetect);
        TargetAddrAcquire targetAddrAcquire = TargetAddrAcquire.getInstance();
        LOGGER.finer(String.format(Locale.ENGLISH, "send service Register Message:%s", serviceRegisterRequestJson));
        boolean isSuccess = targetAddrAcquire.reportRegistryInfo(serviceRegisterRequestJson);
        return isSuccess ? EMPTY : serviceRegisterMessages;
    }
}
