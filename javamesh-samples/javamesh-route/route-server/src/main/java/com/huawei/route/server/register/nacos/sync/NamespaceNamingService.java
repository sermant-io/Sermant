/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.register.nacos.sync;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 命名空间服务，关联NamingService与Listener
 *
 * @author zhouss
 * @since 2021-10-26
 */
@Data
@Builder
public class NamespaceNamingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NamespaceNamingService.class);

    /**
     * 服务实例更新监听器
     */
    private EventListener serviceInstanceListener;

    /**
     * nacos命名服务，针对当前的命名空间
     */
    private NamingService namingService;

    /**
     * 命名空间
     */
    private String namespace;

    /**
     * 订阅服务
     *
     * @param nacosServiceName 服务名
     * @param group 分组名
     */
    public void subscribe(String nacosServiceName, String group) {
        if (namingService == null || serviceInstanceListener == null) {
            return;
        }
        try {
            namingService.subscribe(nacosServiceName, group, serviceInstanceListener);
            // 手动触发事件，由于第一次事件可能不会通知到，因此，此处需手动通知，更新实例列表
            serviceInstanceListener.onEvent(new NamingEvent(NamingUtils.getGroupedName(nacosServiceName, group),
                    null));
        } catch (NacosException e) {
            LOGGER.warn("subscribe service {}, group {} failed by {}", nacosServiceName, group, e.getMessage());
        }
    }

    /**
     * 取消订阅服务
     *
     * @param nacosServiceName nacos服务名
     * @param group 分组
     */
    public void unsubscribe(String nacosServiceName, String group) {
        if (namingService == null || serviceInstanceListener == null) {
            return;
        }
        try {
            namingService.unsubscribe(nacosServiceName, group, serviceInstanceListener);
        } catch (NacosException e) {
            LOGGER.warn("subscribe service {}, group {} failed by {}", nacosServiceName, group, e.getMessage());
        }
    }
}
