/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.repository;

import com.huawei.route.server.controller.entity.ResponseService;
import com.huawei.route.server.entity.AbstractInstance;
import com.huawei.route.server.entity.AbstractService;
import com.huawei.route.server.register.RegisterSyncManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * 主要用于管理查询服务实例，返回对应的实例列表
 *
 * @author zhouss
 * @since 2021-10-15
 */
@Component
public class ServiceInstanceRepository<S extends AbstractService<T>, T extends AbstractInstance> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceInstanceRepository.class);

    @Autowired
    private RegisterSyncManager<S, T> registerSyncManager;

    /**
     * 根据服务名称与ldc查询实例集合
     *
     * @param serviceNames 服务名集合
     * @param ldc ldc
     * @return 服务实例集合
     */
    public List<ResponseService<T>> queryServiceInstance(List<String> serviceNames, String ldc, String tagName) {
        if (CollectionUtils.isEmpty(serviceNames)) {
            return Collections.emptyList();
        }
        final Map<String, S> cachedServiceInfo = registerSyncManager.getRegisterInfo();
        final List<ResponseService<T>> result = new ArrayList<>();
        final Predicate<AbstractInstance> tagMatch = buildMatch(tagName);
        for (String serviceName : serviceNames) {
            final S service = cachedServiceInfo.get(serviceName);
            if (service == null) {
                LOGGER.debug("service {} not exists", serviceName);
                continue;
            }
            final Map<String, List<T>> ldcInstances = service.getLdcInstances();
            if (ldcInstances == null) {
                continue;
            }
            final ResponseService<T> responseService = new ResponseService<>();
            responseService.setServiceName(service.getServiceName());
            responseService.setRegisterType(service.getRegisterType());
            List<T> instances;
            if (StringUtils.isEmpty(ldc)) {
                instances = new ArrayList<>();
                ldcInstances.values().forEach(instances::addAll);
            } else {
                // 获取指定LDC实例列表
                instances = ldcInstances.get(ldc);
            }
            // 标签比对，去除非该标签实例
            instances.removeIf(tagMatch);
            responseService.setInstances(instances);
            result.add(responseService);
        }
        return result;
    }

    private Predicate<AbstractInstance> buildMatch(String tagName) {
        return instance -> {
            if (instance == null) {
                return true;
            }
            boolean isMatch = instance.getPort() <= 0;
            if (!StringUtils.isEmpty(tagName)) {
                isMatch = isMatch && !StringUtils.isEmpty(instance.getTagName())
                        && !StringUtils.equals(tagName, instance.getTagName());
            }
            return isMatch;
        };
    }
}
