package io.sermant.discovery.service.lb.discovery.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.google.common.collect.Maps;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.discovery.config.NacosRegisterConfig;
import io.sermant.discovery.config.RegisterType;
import io.sermant.discovery.entity.DefaultServiceInstance;
import io.sermant.discovery.entity.RegisterContext;
import io.sermant.discovery.entity.ServiceInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class NacosDiscoveryClientTest {

    private NacosRegisterConfig registerConfig = new NacosRegisterConfig();

    private NacosDiscoveryClient nacosDiscoveryClient;

    private NacosServiceManager nacosServiceManager;

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(NacosRegisterConfig.class))
                .thenReturn(registerConfig);
        RegisterContext.INSTANCE.getClientInfo().setServiceId("test");
        nacosDiscoveryClient = new NacosDiscoveryClient();
        nacosServiceManager = new NacosServiceManager();
        Map<String, String> map = new HashMap<>();
        map.put("foo", "123");
        RegisterContext.INSTANCE.getClientInfo().setMeta(map);
    }

    @After
    public void tearDown() throws Exception {
        pluginConfigManagerMockedStatic.close();
    }

    @Test
    public void testRegister() throws NacosException {
        mockNamingService();
        Instance instance = new Instance();
        instance.setIp("1.1.1.1");
        instance.setPort(8080);
        instance.setServiceName("test");

        Map<String, String> metadata = new HashMap<>();
        metadata.put("version", "1.0.0");
        metadata.put("environment", "dev");
        ServiceInstance serviceInstance = new DefaultServiceInstance("127.0.0.1:8080", "127.0.0.1", 8080, metadata
                , "test");
        nacosDiscoveryClient.registry(serviceInstance);
    }

    @Test
    public void testDeregister() throws NacosException {
        mockNamingService();
        Instance instance = new Instance();
        instance.setIp("1.1.1.1");
        instance.setPort(8080);
        instance.setServiceName("test");

        Map<String, Instance> instanceMap = new HashMap<>();
        instanceMap.put("1.1.1.1:8080", instance);

        nacosDiscoveryClient.unRegistry();
    }

    @Test
    public void testGetInstances() throws NacosException {
        mockNamingService();
        nacosDiscoveryClient.getInstances("test");
    }

    @Test
    public void testRegisterType(){
        Assert.assertEquals(nacosDiscoveryClient.registerType(), RegisterType.NACOS);
    }

    @Test
    public void testConvertServiceInstanceList(){
        Instance instance = new Instance();
        instance.setInstanceId("1.1.1.1:8080");
        instance.setWeight(1.0D);
        instance.setHealthy(true);
        instance.setClusterName("DEFAULT");
        instance.setMetadata(Maps.newHashMap());
        instance.setEnabled(true);
        instance.setEphemeral(false);

        instance.setIp("1.1.1.1");
        instance.setPort(8080);
        instance.setServiceName("test");
        List<ServiceInstance> serviceInstances = nacosDiscoveryClient.convertServiceInstanceList(Collections.singletonList(instance), "test");
        Assert.assertEquals(serviceInstances.size(), 1);
    }

    private void mockNamingService() throws NacosException {
        final NamingService namingService = Mockito.mock(NamingService.class);
        List<String> list = new ArrayList<>();
        list.add("test");
        ListView<String> services = new ListView<>();
        services.setData(list);
        final NamingMaintainService namingMaintainService = Mockito.mock(NamingMaintainService.class);
        ReflectUtils.setFieldValue(nacosServiceManager, "namingService", namingService);
        ReflectUtils.setFieldValue(nacosServiceManager, "namingMaintainService", namingMaintainService);
        setNacosServiceManager();
    }

    private void setNacosServiceManager() {
        ReflectUtils.setFieldValue(nacosDiscoveryClient, "nacosServiceManager", nacosServiceManager);
        ReflectUtils.setFieldValue(nacosDiscoveryClient, "nacosServiceDiscovery",
                new NacosDiscoveryClient());
    }
}
