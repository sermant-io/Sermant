/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.register.nacos.sync;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import com.huawei.route.server.constants.NacosConstants;
import com.huawei.route.server.constants.RouteConstants;
import com.huawei.route.server.entity.InstanceHolder;
import com.huawei.route.server.register.nacos.NacosInstance;
import com.huawei.route.server.register.nacos.NacosService;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务实例监听器
 *
 * @author zhouss
 * @since 2021-10-26
 */
@Builder
public class ServiceInstanceListener implements EventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceInstanceListener.class);

    /**
     * 简单的服务名映射
     * 路由Server服务名定义映射与nacos定义的服务名
     * <p>
     * key : 路由Server服务名
     * value : nacos服务名列表
     */
    private final Map<String, Set<String>> routeServiceNameMapper = new ConcurrentHashMap<>();

    /**
     * 简单的服务名映射
     * nacos定义的服务名与路由Server服务名定义映射
     * <p>
     * key : nacos服务名
     * value : routeServer服务
     */
    private final Map<String, String> serviceNameMapper = new ConcurrentHashMap<>();

    @Getter
    @Setter
    private Map<String, NacosService> registerInfo;

    @Getter
    @Setter
    private NamingService namingService;

    @Getter
    @Setter
    private String nacosSeparator;

    @Getter
    @Setter
    private String namespace;

    @Override
    public void onEvent(Event event) {
        if (event instanceof NamingEvent) {
            NamingEvent namingEvent = (NamingEvent) event;
            final String nacosServiceName = namingEvent.getServiceName();
            final List<Instance> instances = getInstances(namingEvent);
            String serviceName;
            if (isDubboService(nacosServiceName)) {
                serviceName = getDubboServiceName(nacosServiceName, instances);
            } else {
                serviceName = getSpringServiceName(nacosServiceName);
            }
            if (StringUtils.isEmpty(serviceName)) {
                return;
            }
            updateMapper(nacosServiceName, serviceName);
            updateNacosService(serviceName, namingEvent, instances);
        }
    }

    private List<Instance> getInstances(NamingEvent namingEvent) {
        try {
            // 从nacos自身缓存查询正确的实例列表
            return namingService.selectInstances(NamingUtils.getServiceName(namingEvent.getServiceName()),
                    NamingUtils.getGroupName(namingEvent.getServiceName()),
                    true, true);
        } catch (NacosException e) {
            LOGGER.debug("select nacos instances failed! ");
            return Collections.emptyList();
        }
    }

    private void updateNacosService(String serviceName, NamingEvent namingEvent, List<Instance> instances) {
        NacosService nacosService = registerInfo.get(serviceName);
        if (nacosService == null) {
            nacosService = new NacosService();
            nacosService.setServiceName(serviceName);
            nacosService.setClusters(namingEvent.getClusters());
            nacosService.setGroup(namingEvent.getGroupName());
        }
        nacosService.setInstanceHolders(convert(instances, serviceName, namingEvent.getServiceName()));
        registerInfo.put(serviceName, nacosService);
    }

    /**
     * 转换nacos原生数据
     *
     * @param instances        原生实例列表
     * @param serviceName      转换后服务名
     * @param nacosServiceName nacos原生服务名
     * @return InstanceHolder
     */
    private Map<String, InstanceHolder<NacosInstance>> convert(List<Instance> instances, String serviceName,
                                                               String nacosServiceName) {
        final NacosService nacosService = registerInfo.get(serviceName);
        // 1、获取实例列表
        Map<String, InstanceHolder<NacosInstance>> instanceHolders = getInstanceHolders(nacosService);
        final String holderKey = getHolderKey(serviceName);
        // 2、获取指定命名空间@服务名的实例列表
        final InstanceHolder<NacosInstance> nacosInstanceInstanceHolder =
                instanceHolders.getOrDefault(holderKey, new InstanceHolder<>());
        // 3、更新数据
        updateInstanceHolder(nacosInstanceInstanceHolder, nacosServiceName, instances, serviceName, holderKey);
        instanceHolders.put(holderKey, nacosInstanceInstanceHolder);
        return instanceHolders;
    }

    private void updateInstanceHolder(InstanceHolder<NacosInstance> nacosInstanceInstanceHolder,
                                      String nacosServiceName,
                                      List<Instance> instances,
                                      String serviceName,
                                      String holderKey) {
        // 获取旧的分组实例列表缓存，其中集合存储的数据为 ip@port
        final Set<String> oldIpGroup = getOldGroupInstances(nacosInstanceInstanceHolder, nacosServiceName);
        final Set<String> newIpGroup = new HashSet<>();
        for (Instance instance : instances) {
            final NacosInstance nacosInstance = buildNacosInstance(nacosInstanceInstanceHolder, instance, serviceName);
            final String instanceKey = nacosInstance.getInstanceKey();
            nacosInstanceInstanceHolder.update(instanceKey, nacosInstance);
            newIpGroup.add(instanceKey);
            oldIpGroup.remove(instanceKey);
        }
        nacosInstanceInstanceHolder.updateServiceGroup(nacosServiceName, newIpGroup);
        // 移除已过期的实例列表
        if (oldIpGroup.size() > 0) {
            for (String leaveInstanceKey : oldIpGroup) {
                nacosInstanceInstanceHolder.remove(leaveInstanceKey);
            }
        }
        nacosInstanceInstanceHolder.setKey(holderKey);
    }

    private Set<String> getOldGroupInstances(InstanceHolder<NacosInstance> nacosInstanceInstanceHolder,
                                             String nacosServiceName) {
        Map<String, Set<String>> serviceGroup = nacosInstanceInstanceHolder.getServiceGroup();
        if (serviceGroup == null) {
            serviceGroup = new HashMap<>();
        }
        return serviceGroup.getOrDefault(nacosServiceName, new HashSet<>());
    }

    private Map<String, InstanceHolder<NacosInstance>> getInstanceHolders(NacosService nacosService) {
        Map<String, InstanceHolder<NacosInstance>> instanceHolders;
        if (nacosService != null) {
            instanceHolders = nacosService.getInstanceHolders();
        } else {
            instanceHolders = new ConcurrentHashMap<>();
        }
        if (instanceHolders == null) {
            instanceHolders = new ConcurrentHashMap<>();
            nacosService.setInstanceHolders(instanceHolders);
        }
        return instanceHolders;
    }

    /**
     * 基于存在的实例更新或者构建nacos实例
     *
     * @param nacosInstanceInstanceHolder 当前的nacos实例列表
     * @param instance                    事件实例，即nacos原生实例
     * @param serviceName                 转换后服务名
     * @return NacosInstance
     */
    private NacosInstance buildNacosInstance(InstanceHolder<NacosInstance> nacosInstanceInstanceHolder,
                                             Instance instance, String serviceName) {
        NacosInstance originInstance = nacosInstanceInstanceHolder
                .getInstance(getInstanceKey(instance.getIp(), instance.getPort()));
        if (originInstance == null) {
            originInstance = new NacosInstance();
        }
        originInstance.setMetadata(resolveMetadata(instance));
        originInstance.setIp(instance.getIp());
        originInstance.setPort(instance.getPort());
        originInstance.setHealth(instance.isHealthy());
        originInstance.setWeight(instance.getWeight());
        originInstance.setValid(instance.isHealthy());
        originInstance.setNamespace(namespace);
        originInstance.setServiceName(serviceName);
        return originInstance;
    }

    /**
     * 解析获取必要的元数据
     *
     * @param instance nacos实例
     *                 instanceId格式如下（Dubbo）：
     *                 10.207.0.164#20880#DEFAULT#DEFAULT_GROUP@@providers:com.huawei.demo.dubbo.nacos.DemoService:4.0.0:groupName
     * @return metadata
     */
    private Map<String, String> resolveMetadata(Instance instance) {
        final Map<String, String> result = new HashMap<>();
        // 集群名称
        result.put(NacosConstants.CLUSTER_NAME, instance.getClusterName());
        result.put(NacosConstants.NAMESPACE, namespace);
        String instanceId = instance.getInstanceId();
        if (StringUtils.isEmpty(instanceId)) {
            return result;
        }
        // 将分为两块 端口分组等数据  接口数据
        final String[] parts = StringUtils.split(instanceId, NacosConstants.NACOS_INSTANCE_ID_SEPARATOR);
        if (parts.length != NacosConstants.INFO_PARTS_LEN) {
            return result;
        }
        // 获取分组数据
        final String[] infoParts = StringUtils.split(parts[0], NacosConstants.INFO_INNER_SEPARATOR);
        result.put(NacosConstants.GROUP, infoParts[infoParts.length - 1]);
        if (isDubboService(instanceId)) {
            // 获取版本数据
            result.put(NacosConstants.VERSION, resolveVersion(parts[1]));
            final Map<String, String> metadata = instance.getMetadata();
            // dubbo的分组存在于metadata中，此处将值覆盖
            result.put(NacosConstants.GROUP,
                    metadata.getOrDefault(NacosConstants.GROUP, infoParts[infoParts.length - 1]));
        }
        return result;
    }

    /**
     * 解析版本
     * 格式： DEFAULT_GROUP@@providers@@com.huawei.hello:4.0.0.0:groupName
     *
     * @param nacosServiceName nacos服务名
     * @return version
     */
    private String resolveVersion(String nacosServiceName) {
        String version = "";
        if (StringUtils.isEmpty(nacosServiceName)) {
            return version;
        }
        final int groupIndex = nacosServiceName.lastIndexOf(nacosSeparator);
        if (groupIndex == -1) {
            return version;
        }
        final int versionIndex = nacosServiceName.lastIndexOf(nacosSeparator, groupIndex - 1);
        if (versionIndex == -1) {
            return version;
        }
        return nacosServiceName.substring(versionIndex + 1, groupIndex);
    }

    /**
     * 判定当前服务是否为dubbo服务
     * dubbo服务分以下梁总格式
     * ‘DEFAULT_GROUP@@providers:com.huawei.demo.dubbo.nacos.DemoService::’
     * ’DEFAULT_GROUP@@consumers:com.huawei.demo.dubbo.nacos.DemoService::‘
     *
     * @return 是否为dubbo服务
     */
    private boolean isDubboService(String serviceName) {
        return StringUtils.contains(serviceName, "@@providers")
                || StringUtils.contains(serviceName, "@@consumers");
    }

    private String getDubboServiceName(String nacosServiceName, List<Instance> instances) {
        final String routeServiceName = serviceNameMapper.get(nacosServiceName);
        if (StringUtils.isNotEmpty(routeServiceName)) {
            return routeServiceName;
        }
        if (CollectionUtils.isEmpty(instances)) {
            return "";
        }
        return instances.get(0).getMetadata().get(NacosConstants.APPLICATION);
    }

    private String getSpringServiceName(String serviceName) {
        final String routeServiceName = serviceNameMapper.get(serviceName);
        if (StringUtils.isNotEmpty(routeServiceName)) {
            return routeServiceName;
        }
        if (StringUtils.isEmpty(serviceName)) {
            return "";
        }
        final int index = serviceName.lastIndexOf(RouteConstants.COMMON_SEPARATOR);
        if (index != -1) {
            return serviceName.substring(index + 1);
        }
        return "";
    }

    /**
     * 更新nacos服务与转换后服务的映射表
     *
     * @param nacosServiceName nacos服务名
     * @param serviceName      转换后的服务名（spring应用直接取服务名，dubbo则取application）
     */
    private void updateMapper(String nacosServiceName, String serviceName) {
        serviceNameMapper.put(nacosServiceName, serviceName);
        final Set<String> services = routeServiceNameMapper.getOrDefault(serviceName, new HashSet<>());
        final int index = nacosServiceName.lastIndexOf(RouteConstants.COMMON_SEPARATOR);
        if (index != -1) {
            services.add(nacosServiceName);
            routeServiceNameMapper.put(serviceName, services);
        }
    }

    private String getInstanceKey(String ip, int port) {
        return ip + RouteConstants.COMMON_SEPARATOR + port;
    }

    private String getHolderKey(String serviceName) {
        return namespace + RouteConstants.COMMON_SEPARATOR + serviceName;
    }

}
