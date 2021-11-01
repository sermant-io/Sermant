/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.register.nacos.sync;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.huawei.route.server.conditions.NacosSyncCondition;
import com.huawei.route.server.config.RouteServerProperties;
import com.huawei.route.server.constants.NacosConstants;
import com.huawei.route.server.entity.ServiceRegistrarMessage;
import com.huawei.route.server.register.AbstractRegisterSync;
import com.huawei.route.server.register.RegisterCenterTypeEnum;
import com.huawei.route.server.register.nacos.NacosInstance;
import com.huawei.route.server.register.nacos.NacosService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 通过监听的方式拿服务实例信息, 增加对多个分组的支持
 *
 * @author zhouss
 * @since 2021-10-20
 */
@Component
@Conditional(NacosSyncCondition.class)
public class NacosSyncByListener extends AbstractRegisterSync<NacosService, NacosInstance> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NacosSyncByListener.class);

    @Autowired
    private RouteServerProperties routeServerProperties;

    @Autowired
    private NacosNamingProxy namingProxyExtension;

    private final Map<String, NacosService> registerInfo = new ConcurrentHashMap<>();

    /**
     *
     * key : namespace
     * value : {@link NamespaceNamingService}
     */
    private final Map<String, NamespaceNamingService> namingServiceMap = new HashMap<>();

    /**
     * 订阅者
     * <p>
     * key: namespace@group
     * value: nacos服务
     */
    private final Map<String, NamespaceGroup> subscribers = new ConcurrentHashMap<>();

    /**
     * 命名空间
     * 默认public
     */
    private final Set<String> namespaces = new HashSet<>(Collections.singleton(NacosConstants.DEFAULT_NACOS_NAMESPACE));

    /**
     * 是否初始化成功
     * 如果未初始化成功，后续会定时尝试连接nacos地址
     */
    private volatile boolean isInitializedSuccess = false;

    @PostConstruct
    public void init() {
        try {
            updateNamespaceNamingService();
            // 第一次初始化的加载初始配置的分组与服务
            resolveConfigurationNamespaceGroup();
            updateSubscribers();
            isInitializedSuccess = true;
        } catch (Exception e) {
            isInitializedSuccess = false;
            LOGGER.warn("init naming service failed, please check your nacos url {}; it will reInit, reason:[{}]",
                    routeServerProperties.getGray().getNacos().getServiceUrl(), e.getMessage());
        }
    }

    @Override
    public Map<String, NacosService> sync() {
        if (isInitializedSuccess) {
            // 定时更新订阅者
            try {
                updateSubscribers();
            } catch (NacosException e) {
                LOGGER.warn("update subscribers failed by {}", e.getMessage());
            }
        } else {
            // 未初始化成功的场景，重新连接进行初始化
            init();
        }
        return registerInfo;
    }

    @Override
    public void update(Collection<ServiceRegistrarMessage> serviceRegistrarMessages) {
        if (CollectionUtils.isEmpty(serviceRegistrarMessages)) {
            return;
        }
        for (ServiceRegistrarMessage serviceRegistrarMessage : serviceRegistrarMessages) {
            if (!StringUtils.equals(serviceRegistrarMessage.getRegistrarServiceName(),
                    RegisterCenterTypeEnum.NACOS.toString())) {
                continue;
            }
            final String namespace = serviceRegistrarMessage.getNamespaceId();
            if (StringUtils.isEmpty(namespace)) {
                continue;
            }
            final String namespaceGroup = getGroupKey(namespace);
            final NamespaceGroup groupService = subscribers.get(namespaceGroup);
            if (groupService == null) {
                // 添加新的服务分组监听
                subscribers.put(namespaceGroup, NamespaceGroup.builder().namespace(namespace).build());
            }
        }
    }

    /**
     * 解析配置的命名空间服务
     */
    private void resolveConfigurationNamespaceGroup() {
        //1.自定义分组处理, 优先使用自定义组，否则使用预先指定的namespace与group
        final String customNamespaces = routeServerProperties.getGray().getNacos().getCustomNamespaceGroup();
        if (StringUtils.isNotEmpty(customNamespaces)) {
            // 切割多组
            final String[] namespaces = StringUtils.split(customNamespaces,
                    NacosConstants.CUSTOM_NACOS_NAMESPACE_SEPARATOR);
            for (String namespace : namespaces) {
                subscribers.put(getGroupKey(namespace), NamespaceGroup.builder().namespace(namespace.trim()).build());
            }
        } else {
            String namespace = routeServerProperties.getGray().getNacos().getNamespaceId();
            subscribers.put(getGroupKey(namespace),
                    NamespaceGroup.builder().namespace(namespace).build());
        }
    }

    private String getGroupKey(String namespace) {
        return namespace;
    }

    private void updateNamespaceNamingService() {
        for (String namespace : namespaces) {
            final NamespaceNamingService namespaceNamingService = getNamingService(namespace);
            if (namespaceNamingService == null) {
                LOGGER.warn("create naming service failed, please check your nacos config!");
            }
        }
    }

    private NamespaceNamingService getNamingService(String namespace) {
        if (StringUtils.isEmpty(namespace)) {
            return null;
        }
        try {
            final NamespaceNamingService namespaceNamingService =
                    namingServiceMap.getOrDefault(namespace, buildNamespaceNamingService(namespace));
            namingServiceMap.put(namespace, namespaceNamingService);
            return namespaceNamingService;
        } catch (NacosException e) {
            return null;
        }
    }

    private NamespaceNamingService buildNamespaceNamingService(String namespace) throws NacosException {
        final NamingService namingService = createNamingService(namespace);
        return NamespaceNamingService.builder()
                .namespace(namespace)
                .namingService(namingService)
                .serviceInstanceListener(createServiceInstanceListener(namespace, namingService))
                .build();
    }

    private EventListener createServiceInstanceListener(String namespace, NamingService namingService) {
        return ServiceInstanceListener.builder()
                .registerInfo(registerInfo)
                .namingService(namingService)
                .nacosSeparator(routeServerProperties.getGray().getNacos().getServiceSeparator())
                .namespace(namespace)
                .build();

    }

    private NamingService createNamingService(String namespace) throws NacosException {
        return NamingFactory.createNamingService(generateProperties(namespace));
    }

    /**
     * 初始化配置
     *
     * @return 初始化配置
     */
    private Properties generateProperties(String namespace) {
        final Properties properties = new Properties();
        // 命名空间
        properties.put(PropertyKeyConst.NAMESPACE, namespace);
        properties.put(PropertyKeyConst.SERVER_ADDR, routeServerProperties.getGray().getNacos().getUrl());
        return properties;
    }

    /**
     * 更新订阅者
     *
     * @throws NacosException 查询nacos服务异常
     */
    private void updateSubscribers() throws NacosException {
        final Set<Map.Entry<String, NamespaceGroup>> entries = subscribers.entrySet();
        for (Map.Entry<String, NamespaceGroup> entry : entries) {
            final NamespaceGroup nameGroup = entry.getValue();
            final String namespace = nameGroup.getNamespace();
            if (StringUtils.isEmpty(namespace)) {
                continue;
            }
            final NamespaceNamingService namingService = getNamingService(namespace);
            if (namingService == null || namingService.getNamingService() == null) {
                continue;
            }
            // 比较更新订阅的服务列表
            compareNameGroups(nameGroup, namingService);
        }
    }

    private void compareNameGroups(NamespaceGroup groupService, NamespaceNamingService namingService) {
        final Map<String, Set<String>> latestServices = queryAllServices(groupService.getNamespace());
        final Map<String, Set<String>> oldServices = groupService.getGroupServices();
        if (CollectionUtils.isEmpty(oldServices)) {
            // 订阅服务
            latestServices.forEach((group, groupServices) ->
                    groupServices.forEach(serviceName -> namingService.subscribe(serviceName, group)));
        } else {
            // 更新
            updateNameGroups(latestServices, oldServices, namingService);
        }
        groupService.setGroupServices(latestServices);
    }

    private void updateNameGroups(Map<String, Set<String>> latestServices, Map<String, Set<String>> oldServices,
                                  NamespaceNamingService namingService) {
        // 对比更新
        latestServices.forEach((group, groupServices) -> {
            final Set<String> services = oldServices.getOrDefault(group, Collections.emptySet());
            for (String service : groupServices) {
                if (services.contains(service)) {
                    services.remove(service);
                    continue;
                }
                namingService.subscribe(service, group);
            }
            // 移除过期的订阅者
            if (!services.isEmpty()) {
                services.forEach(leaveService -> namingService.unsubscribe(leaveService, group));
            }
        });
    }

    private Map<String, Set<String>> queryAllServices(String namespace) {
        final List<ServiceInfo> serviceInfos = namingProxyExtension.queryHealthServices(namespace);
        return serviceInfos.stream()
                .collect(Collectors.groupingBy(
                        ServiceInfo::getGroupName,
                        Collectors.mapping(ServiceInfo::getName, Collectors.toSet())));
    }
}
