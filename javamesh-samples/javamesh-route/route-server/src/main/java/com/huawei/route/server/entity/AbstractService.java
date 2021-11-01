package com.huawei.route.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.huawei.route.server.register.RegisterCenterTypeEnum;
import com.huawei.route.server.rules.GrayRuleManager;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务信息
 *
 * @author zhouss
 * @since 2021-10-08
 */
@Getter
@Setter
public abstract class AbstractService<T extends AbstractInstance> {
    /**
     * 注册中心类型
     * nacos
     * zookeeper
     * serviceComb
     */
    private RegisterCenterTypeEnum registerType;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 所属机房
     */
    private String ldc;

    /**
     * 该服务的实例列表, 注册中心同步数据
     * key: 各实现自定义键
     *      nacos: namespace@serviceName
     * value: 针对该key的实例列表
     */
    @JsonIgnore
    private Map<String, InstanceHolder<T>> instanceHolders;

    /**
     * 实例集合, 以ldc进行分类
     * key : ldc
     */
    public Map<String, List<T>> ldcInstances;

    /**
     * 上次更新心跳
     */
    private long lastHeartbeat;

    protected AbstractService(RegisterCenterTypeEnum registerType) {
        this.registerType = registerType;
        instanceHolders = new ConcurrentHashMap<>();
        ldcInstances = new HashMap<>();
    }

    /**
     * 更新自身实例配置
     *
     * @param instanceTagConfigurations 实例配置数据
     * @param instanceNameIpMapper 实例名与IP映射数据
     */
    public void updateInstanceConfiguration(
            List<GrayRuleManager.RawTagInstanceConfiguration> instanceTagConfigurations,
            Map<String, InstanceHeartBeat> instanceNameIpMapper) {
        for (GrayRuleManager.RawTagInstanceConfiguration rawTagInstanceConfiguration : instanceTagConfigurations) {
            final InstanceHeartBeat instanceHeartBeat = instanceNameIpMapper
                    .get(rawTagInstanceConfiguration.getInstanceName());
            if (instanceHeartBeat == null) {
                continue;
            }
            final T instance = matchInstance(instanceHeartBeat.getInstanceKey());
            if (instance != null) {
                if (rawTagInstanceConfiguration.isValid()) {
                    instance.setInstanceTagConfiguration(rawTagInstanceConfiguration.getInstanceTagConfiguration());
                } else {
                    instance.setInstanceTagConfiguration(null);
                }
            }
        }
    }

    private T matchInstance(String instanceKey) {
        for (InstanceHolder<T> holder : instanceHolders.values()) {
            final T instance = holder.getInstances().get(instanceKey);
            if (instance != null) {
                return instance;
            }
        }
        return null;
    }

}
