package io.sermant.discovery.service.lb.discovery.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.discovery.config.NacosRegisterConfig;
import io.sermant.discovery.entity.DefaultServiceInstance;
import io.sermant.discovery.entity.RegisterContext;
import io.sermant.discovery.entity.ServiceInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NacosDiscoveryClientTest {

    @Mock
    private NamingService namingService;

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
    }

    @Test
    public void testRegister() throws NacosException {
        mockNamingService();
        Instance instance = new Instance();
        instance.setIp("1.1.1.1");
        instance.setPort(8080);
        instance.setServiceName("test");

        when(namingService.selectInstances(anyString(), any(Boolean.class), any(Boolean.class)))
                .thenReturn(Arrays.asList(instance));

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

        when(namingService.selectInstances(anyString(), any(Boolean.class), any(Boolean.class)))
                .thenReturn(Arrays.asList(instance));

        nacosDiscoveryClient.unRegistry();
    }


    private void mockNamingService() throws NacosException {
        final NamingService namingService = Mockito.mock(NamingService.class);
        List<String> list = new ArrayList<>();
        list.add("test");
        ListView<String> services = new ListView<>();
        services.setData(list);
        Mockito.when(namingService.getServicesOfServer(1, Integer.MAX_VALUE, registerConfig.getGroup()))
                .thenReturn(services);
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
