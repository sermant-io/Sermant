/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.report.send;


import com.huawei.route.common.report.common.entity.ServiceRegisterMessage;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 服务注册信息发送
 *
 * @author zhengbin zhao
 * @version 1.0
 * @since 2021-07-15
 */
public interface ServiceRegistrarMessageSender {
    /**
     * 由于JDK1.6不支持diamond, 因此提供一个默认的空set，防止多次创建
     */
    Set<ServiceRegisterMessage> EMPTY = new HashSet<ServiceRegisterMessage>();

    /**
     * 服务上报数据发送
     *
     * @param serviceRegisterMessages 上报数据
     * @return 上报遗留数据
     * @throws IOException 发送失败触发
     */
    Set<ServiceRegisterMessage> sendServiceRegisterMessage(Set<ServiceRegisterMessage> serviceRegisterMessages)
        throws IOException;
}
