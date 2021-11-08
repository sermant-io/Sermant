/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.repository;

import com.huawei.route.server.entity.AbstractInstance;
import com.huawei.route.server.entity.AbstractService;
import com.huawei.route.server.register.RegisterSyncManager;
import com.huawei.route.server.rules.InstanceTagConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 主要用于管理查询服务实例，返回对应的实例列表
 *
 * @author zhouss
 * @since 2021-10-15
 */
@Component
public class InstanceRuleRepository<S extends AbstractService<T>, T extends AbstractInstance> {
    @Autowired
    private RegisterSyncManager<S, T> registerSyncManager;

    /**
     * 查询单个实例的实例规则
     *
     * @param ip 实例IP
     * @param port 实例端口
     * @return 实例标签规则
     */
    public InstanceTagConfiguration queryInstanceRule(String ip, int port) {
        final Map<String, S> cachedServiceInfo = registerSyncManager.getRegisterInfo();
        for (Map.Entry<String, S> entry : cachedServiceInfo.entrySet()) {
            final S service = entry.getValue();
            final Map<String, List<T>> ldcInstances = service.getLdcInstances();
            if (ldcInstances == null) {
                continue;
            }
            for (List<T> instances : ldcInstances.values()) {
                final Optional<T> anyInstance = instances.stream()
                        .filter(instance -> StringUtils.pathEquals(instance.getIp(), ip) && instance.getPort() == port)
                        .findAny();
                if (anyInstance.isPresent()) {
                    return anyInstance.get().getInstanceTagConfiguration();
                }
            }
        }
        return null;
    }
}
