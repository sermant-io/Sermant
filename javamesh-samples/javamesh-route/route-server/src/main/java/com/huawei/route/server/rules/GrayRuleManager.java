/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.rules;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.huawei.route.common.RouteThreadFactory;
import com.huawei.route.server.config.RouteServerProperties;
import com.huawei.route.server.constants.RouteConstants;
import com.huawei.route.server.entity.AbstractInstance;
import com.huawei.route.server.entity.AbstractService;
import com.huawei.route.server.entity.ServiceRegistrarMessage;
import com.huawei.route.server.labels.label.service.LabelService;
import com.huawei.route.server.register.RegisterSyncManager;
import com.huawei.route.server.share.RouteSharer;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 灰度规则管理器，管理与同步灰度规则数据
 *
 * @author zhouss
 * @since 2021-10-13
 */
@Component
public class GrayRuleManager<S extends AbstractService<T>, T extends AbstractInstance> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrayRuleManager.class);

    private LabelService labelService;

    @Autowired
    public void setLabelService(LabelService labelService) {
        this.labelService = labelService;
    }

    @Autowired
    private RegisterSyncManager<S, T> registerSyncManager;

    private RouteServerProperties routeServerProperties;

    @Autowired
    private void setRouteServerProperties(RouteServerProperties routeServerProperties) {
        this.routeServerProperties = routeServerProperties;
    }

    @Resource(name = "tagInstanceIpMapper")
    private TagInstanceIpMapper tagInstanceIpMapper;

    @Resource(name = "redisRouteSharer")
    private RouteSharer<ServiceRegistrarMessage> redisRouteSharer;

    private List<RawTagServiceConfiguration> cachedServiceConfigurations;

    private volatile boolean isConfigurationInit = false;

    private ThreadPoolExecutor initThread;

    @PostConstruct
    @SuppressWarnings("all")
    public void syncInstanceTagConfiguration() {
        initThread = new ThreadPoolExecutor(1, 1,
                RouteConstants.INIT_WAIT_MS,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(1),
                new RouteThreadFactory("RULE_SYNC_THREAD"));
        initThread.execute(() -> {
            try {
                // 防止初始化时未接收到心跳数据而导致配置均为空
                while (!isConfigurationInit) {
                    Thread.sleep(RouteConstants.INIT_WAIT_MS);
                    updateTagConfiguration(null, null);
                    if (isConfigurationInit) {
                        break;
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("update instance tag configuration failed!", e);
            }
        });
    }

    @PreDestroy
    public void destroy() {
        initThread.shutdown();
    }

    /**
     * 更新实例标签数据
     *
     * @param serviceName  标签服务名
     * @param instanceName 标签实例名
     */
    public void updateTagConfiguration(String serviceName, String instanceName) {
        if (StringUtils.isEmpty(serviceName) || StringUtils.isEmpty(instanceName)) {
            updateAllInstanceTagConfiguration();
            relateTagToInstance();
        } else {
            updateSingleInstanceConfiguration(serviceName, instanceName);
        }
    }

    /**
     * 更新指定服务实例的标签配置
     *
     * @param serviceName  标签服务名
     * @param instanceName 标签实例名
     */
    private void updateSingleInstanceConfiguration(String serviceName, String instanceName) {
        final Map<String, S> registerInfo = registerSyncManager.getRegisterInfo();
        final Map<String, Set<String>> serviceMapper = tagInstanceIpMapper.getServiceMapper();
        final Set<String> registerServices = serviceMapper.get(serviceName);
        if (CollectionUtils.isEmpty(registerServices)) {
            return;
        }
        final JSONObject instanceConfigurationJson = labelService.selectSingleInstance(serviceName, instanceName,
                routeServerProperties.getGray().getRouteGroupName(), routeServerProperties.getGray().getGrayLabelName());
        final RawTagInstanceConfiguration rawTagInstanceConfiguration =
                JSONObject.parseObject(instanceConfigurationJson.toJSONString(), RawTagInstanceConfiguration.class);
        rawTagInstanceConfiguration.setInstanceTagConfiguration(
                InstanceTagConfiguration.convert(rawTagInstanceConfiguration.getValue()));
        rawTagInstanceConfiguration.setValue(null);
        // 更新实例配置
        final List<RawTagInstanceConfiguration> rawTagInstanceConfigurations =
                Collections.singletonList(rawTagInstanceConfiguration);
        updateRegisterServices(registerServices, registerInfo, rawTagInstanceConfigurations);
    }

    /**
     * 更新所有实例规则，并与LDC关联
     */
    private void updateAllInstanceTagConfiguration() {
        final JSONArray jsonArray = labelService.selectRawLabelInstance(
                routeServerProperties.getGray().getRouteGroupName(), routeServerProperties.getGray().getGrayLabelName());
        if (jsonArray == null || jsonArray.size() == 0) {
            LOGGER.debug("no instance had been found");
            return;
        }
        final List<RawTagServiceConfiguration> serviceConfigurations =
                JSONArray.parseArray(jsonArray.toJSONString(), RawTagServiceConfiguration.class);
        if (CollectionUtils.isEmpty(serviceConfigurations)) {
            return;
        }
        // 转换字符串的标签配置为实体
        for (RawTagServiceConfiguration rawTagServiceConfiguration : serviceConfigurations) {
            final List<RawTagInstanceConfiguration> instanceNames = rawTagServiceConfiguration.getInstanceTagConfigurations();
            if (CollectionUtils.isEmpty(instanceNames)) {
                continue;
            }
            for (RawTagInstanceConfiguration rawTagInstanceConfiguration : instanceNames) {
                rawTagInstanceConfiguration.setInstanceTagConfiguration(
                        InstanceTagConfiguration.convert(rawTagInstanceConfiguration.getValue()));
                // 清空原字符串数据
                rawTagInstanceConfiguration.setValue(null);
            }
        }
        cachedServiceConfigurations = serviceConfigurations;
    }

    /**
     * 关联标签到实例
     */
    private void relateTagToInstance() {
        final Map<String, S> registerInfo = registerSyncManager.getRegisterInfo();
        final Map<String, Set<String>> serviceMapper = tagInstanceIpMapper.getServiceMapper();
        if (cachedServiceConfigurations == null || registerInfo.size() == 0 || serviceMapper.size() == 0) {
            return;
        }
        // 更新实例配置
        for (RawTagServiceConfiguration rawTagServiceConfiguration : cachedServiceConfigurations) {
            final Set<String> registerServices = serviceMapper.get(rawTagServiceConfiguration.getServiceName());
            if (CollectionUtils.isEmpty(registerServices)) {
                continue;
            }
            updateRegisterServices(registerServices, registerInfo,
                    rawTagServiceConfiguration.getInstanceTagConfigurations());
        }
        isConfigurationInit = true;
        cachedServiceConfigurations.clear();
    }

    private void updateRegisterServices(Set<String> registerServices,
                                        Map<String, S> registerInfo,
                                        List<RawTagInstanceConfiguration> instanceTagConfigurations) {
        for (String registerServiceName : registerServices) {
            // 通过标签服务名获取注册中心服务名
            final S service = registerInfo.get(registerServiceName);
            if (service == null) {
                continue;
            }
            service.updateInstanceConfiguration(instanceTagConfigurations, tagInstanceIpMapper.getInstanceNameIpMapper());
        }
    }

    /**
     * 更新路由上报数据
     *
     * @param serviceRegistrarMessageList 上报信息
     */
    public void updateRegistrarMessage(Collection<ServiceRegistrarMessage> serviceRegistrarMessageList) {
        registerSyncManager.update(serviceRegistrarMessageList);
        redisRouteSharer.share(serviceRegistrarMessageList.toArray(new ServiceRegistrarMessage[0]));
    }

    /**
     * 标签库中 服务与实例的配置
     */
    @Getter
    @Setter
    static class RawTagServiceConfiguration {
        private String serviceName;

        @JSONField(name = "instanceNames")
        private List<RawTagInstanceConfiguration> instanceTagConfigurations;
    }

    @Getter
    @Setter
    public static class RawTagInstanceConfiguration {
        private String instanceName;

        private String value;

        private String on;

        private InstanceTagConfiguration instanceTagConfiguration;

        public boolean isValid() {
            return !StringUtils.equalsIgnoreCase(on, "false")
                    && StringUtils.isNotEmpty(instanceName)
                    && instanceTagConfiguration != null
                    && instanceTagConfiguration.isValid();
        }
    }
}
