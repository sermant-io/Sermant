package io.sermant.discovery.service.lb.discovery.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.utils.StringUtils;
import io.sermant.discovery.config.NacosRegisterConfig;
import io.sermant.discovery.entity.RegisterContext;
import io.sermant.discovery.entity.ServiceInstance;
import io.sermant.discovery.service.lb.discovery.ServiceDiscoveryClient;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * nacos注册插件配置
 *
 * @author xiaozhao
 * @since 2024-11-16
 */
public class NacosDiscoveryClient implements ServiceDiscoveryClient {


    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final NacosServiceManager nacosServiceManager = NacosServiceManager.getInstance();

    private Instance instance;

    private final NacosRegisterConfig nacosRegisterConfig;

    public NacosDiscoveryClient() {
        nacosRegisterConfig = PluginConfigManager.getPluginConfig(NacosRegisterConfig.class);
    }

    @Override
    public void init() {

    }

    @Override
    public boolean registry(ServiceInstance serviceInstance) {
        String serviceId = serviceInstance.getServiceName();
        String group = nacosRegisterConfig.getGroup();
        instance = nacosServiceManager.buildNacosInstanceFromRegistration(serviceInstance);
        try {
            NamingService namingService = nacosServiceManager.getNamingService();
            namingService.registerInstance(serviceId, group, instance);
            return true;
        } catch (NacosException e) {
            LOGGER.log(Level.SEVERE, String.format(Locale.ENGLISH, "failed when registry service，serviceId={%s}",
                    serviceId), e);
        }
        return false;
    }


    @Override
    public Collection<String> getServices() {
        try {
            String group = nacosRegisterConfig.getGroup();
            NamingService namingService = nacosServiceManager.getNamingService();
            ListView<String> services = namingService.getServicesOfServer(1, Integer.MAX_VALUE, group);
            return services.getData();
        } catch (NacosException e) {
            LOGGER.log(Level.SEVERE, String.format(Locale.ENGLISH, "getServices failed，"
                    + "isFailureToleranceEnabled={%s}", nacosRegisterConfig.isFailureToleranceEnabled()), e);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean unRegistry() {
        if (StringUtils.isEmpty(RegisterContext.INSTANCE.getClientInfo().getServiceId())) {
            LOGGER.warning("No service to de-register for nacos client...");
            return false;
        }
        String serviceId = RegisterContext.INSTANCE.getClientInfo().getServiceId();
        String group = nacosRegisterConfig.getGroup();
        try {
            NamingService namingService = nacosServiceManager.getNamingService();
            namingService.deregisterInstance(serviceId, group, instance);
            return true;
        } catch (NacosException e) {
            LOGGER.log(Level.SEVERE, String.format(Locale.ENGLISH, "failed when deRegister service，"
                    + "serviceId={%s}", serviceId), e);
        }
        return false;
    }

    @Override
    public String name() {
        return "Nacos";
    }

    @Override
    public void close() throws IOException {
    }


    /**
     * 获取对应服务名微服务实例信息
     *
     * @param serviceId 服务id
     * @return 服务信息
     */
    public List<ServiceInstance> getInstances(String serviceId) {
        String group = nacosRegisterConfig.getGroup();
        try {
            NamingService namingService = nacosServiceManager.getNamingService();
            List<Instance> instances = namingService.selectInstances(serviceId, group, true);
            return convertServiceInstanceList(instances, serviceId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format(Locale.ENGLISH, "failed get Instances，"
                    + "serviceId={%s}", serviceId), e);
        }
        return Collections.emptyList();
    }

    public List<ServiceInstance> convertServiceInstanceList(List<Instance> instances, String serviceId) {
        List<ServiceInstance> result = new ArrayList<>(instances.size());
        for (Instance instance : instances) {
            Optional<ServiceInstance> optional = nacosServiceManager.convertServiceInstance(instance, serviceId);
            optional.ifPresent(result::add);
        }
        return result;
    }



}
