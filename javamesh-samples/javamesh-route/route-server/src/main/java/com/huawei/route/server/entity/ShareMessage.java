/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 分享数据
 *
 * @author zhouss
 * @since 2021-10-18
 */
@Data
@NoArgsConstructor
public class ShareMessage {
    private Set<BaseRegistrarMessage> registrarMessages;

    public ShareMessage(List<BaseRegistrarMessage> messages) {
        addAll(messages);
    }

    /**
     * 将基础数据转换
     *
     * @return 转换为上报格式数据
     */
    public Collection<ServiceRegistrarMessage> convert() {
        if (CollectionUtils.isEmpty(registrarMessages)) {
            return Collections.emptyList();
        }
        final Set<ServiceRegistrarMessage> result = new HashSet<>();
        for (BaseRegistrarMessage baseRegistrarMessage : registrarMessages) {
            final ServiceRegistrarMessage serviceRegistrarMessage = new ServiceRegistrarMessage();
            BeanUtils.copyProperties(baseRegistrarMessage, serviceRegistrarMessage);
            result.add(serviceRegistrarMessage);
        }
        return result;
    }

    /**
     * 添加上报数据
     *
     * @param messages 上报数据集合
     */
    public void addAll(List<BaseRegistrarMessage> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return;
        }
        if (CollectionUtils.isEmpty(registrarMessages)) {
            registrarMessages = new HashSet<>();
        }
        registrarMessages.addAll(messages);
    }
}
