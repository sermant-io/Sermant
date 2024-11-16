package io.sermant.discovery.service.lb.discovery.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.NacosNamingMaintainService;
import com.alibaba.nacos.client.naming.NacosNamingService;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.discovery.config.NacosRegisterConfig;
import io.sermant.discovery.entity.DefaultServiceInstance;
import io.sermant.discovery.entity.ServiceInstance;

import java.util.*;

/**
 * nacos注册插件配置
 *
 * @author xiaozhao
 * @since 2024-11-16
 */
public class NacosServiceManager {

    private static final int DEFAULT_CAPACITY = 16;

    private volatile NamingService namingService;

    private volatile NamingMaintainService namingMaintainService;

    private final NacosRegisterConfig nacosRegisterConfig;


    private static NacosServiceManager nacosServiceManager;

    /**
     * 构造方法
     */
    public NacosServiceManager() {
        nacosRegisterConfig = PluginConfigManager.getPluginConfig(NacosRegisterConfig.class);
    }

    public static NacosServiceManager getInstance() {
        if (nacosServiceManager == null) {
            synchronized (NacosServiceManager.class) {
                if (nacosServiceManager == null) {
                    nacosServiceManager = new NacosServiceManager();
                }
            }
        }
        return nacosServiceManager;
    }


    /**
     * 获取注册服务
     *
     * @return NamingService服务
     * @throws NacosException nacos异常
     */
    public NamingService getNamingService() throws NacosException {
        if (Objects.isNull(this.namingService)) {
            buildNamingService(nacosRegisterConfig.getNacosProperties());
        }
        return namingService;
    }

    /**
     * 获取namingMaintain服务
     *
     * @return namingMaintain服务
     * @throws NacosException nacos异常
     */
    public NamingMaintainService getNamingMaintainService() throws NacosException {
        if (Objects.isNull(namingMaintainService)) {
            buildNamingMaintainService(nacosRegisterConfig.getNacosProperties());
        }
        return namingMaintainService;
    }

    private void buildNamingMaintainService(Properties properties) throws NacosException {
        if (Objects.isNull(namingMaintainService)) {
            synchronized (NacosServiceManager.class) {
                if (Objects.isNull(namingMaintainService)) {
                    namingMaintainService = createNamingMaintainService(properties);
                }
            }
        }
    }

    private void buildNamingService(Properties properties) throws NacosException {
        if (Objects.isNull(namingService)) {
            synchronized (NacosServiceManager.class) {
                if (Objects.isNull(namingService)) {
                    namingService = createNewNamingService(properties);
                }
            }
        }
    }

    private NamingService createNewNamingService(Properties properties) throws NacosException {
        return new NacosNamingService(properties);
    }

    private NamingMaintainService createNamingMaintainService(Properties properties) throws NacosException {
        return new NacosNamingMaintainService(properties);
    }

    /**
     * 构建nacos注册实例
     *
     * @return 实例
     */
    public Instance buildNacosInstanceFromRegistration(ServiceInstance serviceInstance) {
        Instance instance = new Instance();
        instance.setIp(serviceInstance.getIp());
        instance.setPort(serviceInstance.getPort());
        instance.setWeight(nacosRegisterConfig.getWeight());
        instance.setClusterName(nacosRegisterConfig.getClusterName());
        instance.setEnabled(nacosRegisterConfig.isInstanceEnabled());
        final HashMap<String, String> metadata = new HashMap<>(serviceInstance.getMetadata());
        instance.setMetadata(metadata);
        instance.setEphemeral(nacosRegisterConfig.isEphemeral());
        return instance;
    }

    /**
     * 实例信息转换
     *
     * @param instance  服务实例
     * @param serviceId 服务id
     * @return 转换后实例信息
     */
    public Optional<ServiceInstance> convertServiceInstance(Instance instance, String serviceId) {
        if (instance == null || !instance.isEnabled() || !instance.isHealthy()) {
            return Optional.empty();
        }
        DefaultServiceInstance nacosServiceInstance = new DefaultServiceInstance();
        nacosServiceInstance.setHost(instance.getIp());
        nacosServiceInstance.setIp(instance.getIp());
        nacosServiceInstance.setPort(instance.getPort());
        nacosServiceInstance.setServiceName(serviceId);
        nacosServiceInstance.setId(instance.getIp() + ":" + instance.getPort());

        Map<String, String> metadata = new HashMap<>(DEFAULT_CAPACITY);
        metadata.put("nacos.instanceId", instance.getInstanceId());
        metadata.put("nacos.weight", instance.getWeight() + "");
        metadata.put("nacos.healthy", instance.isHealthy() + "");
        metadata.put("nacos.cluster", instance.getClusterName() + "");
        if (instance.getMetadata() != null) {
            metadata.putAll(instance.getMetadata());
        }
        metadata.put("nacos.ephemeral", String.valueOf(instance.isEphemeral()));
        nacosServiceInstance.setMetadata(metadata);

        return Optional.of(nacosServiceInstance);
    }
}
