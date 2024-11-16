package io.sermant.discovery.service.lb.discovery.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import io.sermant.discovery.entity.DefaultServiceInstance;
import io.sermant.discovery.entity.ServiceInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NacosDiscoveryClientTest {

    @Mock
    private NamingService namingService;


    @InjectMocks
    private NacosDiscoveryClient nacosDiscoveryClient;

    private AutoCloseable closeable;

    @Before
    public void setUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);
        nacosDiscoveryClient = new NacosDiscoveryClient();
    }

    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testRegister() throws NacosException {
        Instance instance = new Instance();
        instance.setIp("1.1.1.1");
        instance.setPort(8080);
        instance.setServiceName("test");

        when(namingService.selectInstances(anyString(), any(Boolean.class), any(Boolean.class)))
                .thenReturn(Arrays.asList(instance));

        ServiceInstance serviceInstance = new DefaultServiceInstance();

        instance.setInstanceId("127.0.0.1:8080");
        instance.setPort(8080);
        instance.setWeight(1.0);
        instance.setHealthy(true);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("version", "1.0.0");
        metadata.put("environment", "dev");
        instance.setMetadata(metadata);

        nacosDiscoveryClient.registry(serviceInstance);

        verify(namingService, times(1)).registerInstance(anyString(), any(Instance.class));
    }

    @Test
    public void testDeregister() throws NacosException {
        Instance instance = new Instance();
        instance.setIp("1.1.1.1");
        instance.setPort(8080);
        instance.setServiceName("test");

        Map<String, Instance> instanceMap = new HashMap<>();
        instanceMap.put("1.1.1.1:8080", instance);

        when(namingService.selectInstances(anyString(), any(Boolean.class), any(Boolean.class)))
                .thenReturn(Arrays.asList(instance));

        nacosDiscoveryClient.unRegistry();

        verify(namingService, times(1)).deregisterInstance(anyString(), any(Instance.class));
    }
}
