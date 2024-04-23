/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sermant.dubbo.registry;

import io.sermant.core.config.ConfigManager;
import io.sermant.core.operation.OperationManager;
import io.sermant.core.operation.converter.api.YamlConverter;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.plugin.config.ServiceMeta;
import io.sermant.core.service.ServiceManager;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.dubbo.registry.cache.DubboCache;
import io.sermant.dubbo.registry.listener.GovernanceConfigListener;
import io.sermant.dubbo.registry.service.GovernanceService;
import io.sermant.dubbo.registry.service.RegistryService;
import io.sermant.dubbo.registry.service.RegistryServiceImpl;
import io.sermant.implement.operation.converter.YamlConverterImpl;
import io.sermant.registry.config.RegisterConfig;
import io.sermant.registry.config.RegisterServiceCommonConfig;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.servicecomb.service.center.client.DiscoveryEvents.InstanceChangedEvent;
import org.apache.servicecomb.service.center.client.RegistrationEvents.HeartBeatEvent;
import org.apache.servicecomb.service.center.client.RegistrationEvents.MicroserviceInstanceRegistrationEvent;
import org.apache.servicecomb.service.center.client.RegistrationEvents.MicroserviceRegistrationEvent;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.ServiceCenterDiscovery;
import org.apache.servicecomb.service.center.client.ServiceCenterRegistration;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstancesResponse;
import org.apache.servicecomb.service.center.client.model.MicroservicesResponse;
import org.apache.servicecomb.service.center.client.model.SchemaInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Test RegistryServiceImpl
 *
 * @author provenceee
 * @since 2022-09-19
 */
public class RegistryServiceTest {
    private static final URL CONSUMER_URL = URL.valueOf
        ("consumer://localhost:8081/io.sermant.foo.BarTest?application=dubbo-provider&interface=io.sermant.foo.BarTest");

    private static final URL PROVIDER_URL = URL.valueOf
        ("dubbo://localhost:8081/io.sermant.foo.BarTest?application=dubbo-provider&interface=io.sermant.foo.BarTest");

    private static final Set<MockedStatic<?>> MOCKED_STATICS = new HashSet<>();

    /**
     * Perform mock before the UT is executed
     */
    @BeforeAll
    public static void mock() {
        DubboCache.INSTANCE.setUrlClass(URL.class);
        MockedStatic<ServiceManager> mockServiceManager = Mockito.mockStatic(ServiceManager.class);
        mockServiceManager.when(() -> ServiceManager.getService(GovernanceService.class))
            .thenReturn(new GovernanceService());
        MOCKED_STATICS.add(mockServiceManager);

        RegisterConfig registerConfig = new RegisterConfig();
        registerConfig.setSslEnabled(true);
        registerConfig.setHeartbeatInterval(1);
        registerConfig.setHeartbeatRetryTimes(1);
        registerConfig.setPullInterval(1);
        registerConfig.setInterfaceKeys(Collections.singletonList("foo"));
        MockedStatic<PluginConfigManager> mockPluginConfigManager = Mockito.mockStatic(PluginConfigManager.class);
        mockPluginConfigManager.when(() -> PluginConfigManager.getPluginConfig(RegisterConfig.class))
            .thenReturn(registerConfig);

        RegisterServiceCommonConfig commonConfig = new RegisterServiceCommonConfig();
        mockPluginConfigManager.when(() -> PluginConfigManager.getPluginConfig(RegisterServiceCommonConfig.class))
                .thenReturn(commonConfig);
        MOCKED_STATICS.add(mockPluginConfigManager);

        MockedStatic<ConfigManager> mockConfigManager = Mockito.mockStatic(ConfigManager.class);
        mockConfigManager.when(() -> ConfigManager.getConfig(ServiceMeta.class)).thenReturn(new ServiceMeta());
        MOCKED_STATICS.add(mockConfigManager);
        MockedStatic<OperationManager> operationManagerMockedStatic = Mockito.mockStatic(OperationManager.class);
        operationManagerMockedStatic.when(() -> OperationManager.getOperation(YamlConverter.class))
            .thenReturn(new YamlConverterImpl());
        MOCKED_STATICS.add(operationManagerMockedStatic);
    }

    /**
     * Release the mock object after the UT is executed
     */
    @AfterAll
    public static void close() {
        MOCKED_STATICS.forEach(MockedStatic::close);
        DubboCache.INSTANCE.setServiceName(null);
        DubboCache.INSTANCE.setUrlClass(null);
    }

    /**
     * Test the main method of registration
     */
    @Test
    public void testRegistration() {
        RegistryServiceImpl service = new RegistryServiceImpl();

        // When the test does not load SC
        service.startRegistration();
        Assertions.assertNull(getFieldValue(service, "serviceCenterRegistration", Object.class));

        // Load the sc
        DubboCache.INSTANCE.loadSc();
        Assertions.assertTrue(DubboCache.INSTANCE.isLoadSc());

        // When the URL is a consumer
        service.addRegistryUrls(CONSUMER_URL);
        List<?> registryUrls = getFieldValue(service, "registryUrls", List.class);
        Assertions.assertNotNull(registryUrls);
        Assertions.assertEquals(0, registryUrls.size());

        // URL is the producer
        service.addRegistryUrls(PROVIDER_URL);
        Assertions.assertNotNull(registryUrls);
        Assertions.assertEquals(1, registryUrls.size());

        // Test when SC is loaded
        DubboCache.INSTANCE.setServiceName("dubbo-provider");
        service.startRegistration();

        // Wait time to cancel registration
        mockServiceCenterClient(service);
        service.onMicroserviceInstanceRegistrationEvent(new MicroserviceInstanceRegistrationEvent(true));
        Assertions.assertTrue((Boolean) ReflectUtils.getFieldValue(service, "isRegistrationInProgress").orElse(false));

        // Test the onMicroserviceRegistrationEvent method
        service.onMicroserviceRegistrationEvent(new MicroserviceRegistrationEvent(true));
        ServiceCenterDiscovery serviceCenterDiscovery = getFieldValue(service, "serviceCenterDiscovery",
            ServiceCenterDiscovery.class);
        Assertions.assertTrue((Boolean) ReflectUtils.getFieldValue(service, "isRegistrationInProgress").orElse(false));
        Assertions.assertNotNull(serviceCenterDiscovery);

        // Test calling the onMicroserviceRegistrationEvent method again
        service.onMicroserviceRegistrationEvent(new MicroserviceRegistrationEvent(true));
        Assertions.assertTrue((Boolean) ReflectUtils.getFieldValue(service, "isRegistrationInProgress").orElse(false));
        Assertions.assertNotNull(serviceCenterDiscovery);

        ServiceCenterRegistration serviceCenterRegistration = getFieldValue(service, "serviceCenterRegistration",
            ServiceCenterRegistration.class);
        Assertions.assertNotNull(serviceCenterRegistration);

        // Test shutdown
        Assertions.assertDoesNotThrow(service::shutdown);

        // The test shutdown again
        Assertions.assertDoesNotThrow(service::shutdown);
    }

    /**
     * Test the main method of subscription
     */
    @Test
    public void testSubscribe() {
        TestNotifyListener notifyListener = new TestNotifyListener();
        RegistryService service = new RegistryServiceImpl();
        // URL is the producer
        service.doSubscribe(PROVIDER_URL, notifyListener);
        List<?> events = getFieldValue(service, "PENDING_SUBSCRIBE_EVENT", List.class);
        Assertions.assertNotNull(events);
        Assertions.assertEquals(0, events.size());

        // When the URL is a consumer
        service.doSubscribe(CONSUMER_URL, notifyListener);
        Assertions.assertNotNull(events);
        Assertions.assertEquals(1, events.size());

        // Initialize
        init(service);
        ReflectUtils.setFieldValue(service, "config", new RegisterConfig());
        DubboCache.INSTANCE.setServiceName(null);
        ReflectUtils.setFieldValue(service, "isRegistrationInProgress", false);

        // Subscribe
        service.doSubscribe(CONSUMER_URL, notifyListener);
        List<URL> list = notifyListener.getList();
        Assertions.assertNotNull(list);
        Assertions.assertEquals(1, list.size());
        URL url = list.get(0);
        Assertions.assertEquals("io.sermant.foo.BarTest", url.getServiceKey());
        Assertions.assertEquals("dubbo-provider", url.getParameter("application"));
        Assertions.assertEquals("tag1", url.getParameter("service.meta.parameters.tag"));
        Assertions.assertEquals("1.0.0", url.getParameter("service.meta.version"));

        // Test GovernanceConfigListener
        GovernanceConfigListener listener = new GovernanceConfigListener();
        ReflectUtils.setFieldValue(listener, "registryService", service);
        String content = "{\"providerInfos\":[{\"serviceName\":\"dubbo-provider\",\"schemaInfos\":[{\"schemaId\":\"io.sermant.foo.BarTest\",\"parameters\":{\"timeout\":5000}}]}]}";
        DynamicConfigEvent event = new DynamicConfigEvent("dubbo.servicecomb.governance", "", content,
            DynamicConfigEventType.CREATE);
        listener.process(event);
        List<URL> newList = notifyListener.getList();
        Assertions.assertNotNull(newList);
        Assertions.assertEquals(1, newList.size());
        URL newUrl = newList.get(0);
        Assertions.assertEquals("io.sermant.foo.BarTest", newUrl.getServiceKey());
        Assertions.assertEquals("dubbo-provider", newUrl.getParameter("application"));
        Assertions.assertEquals("tag1", newUrl.getParameter("service.meta.parameters.tag"));
        Assertions.assertEquals("1.0.0", newUrl.getParameter("service.meta.version"));
        Assertions.assertEquals("5000", newUrl.getParameter("timeout"));
    }

    /**
     * Test the onHeartBeatEvent method
     */
    @Test
    public void testOnHeartBeatEvent() throws NoSuchFieldException, IllegalAccessException {
        // Clear data
        Field field = RegistryServiceImpl.class.getDeclaredField("PENDING_SUBSCRIBE_EVENT");
        field.setAccessible(true);
        List<?> list = (List<?>) field.get(null);
        list.clear();

        // Test
        RegistryServiceImpl service = new RegistryServiceImpl();
        service.onHeartBeatEvent(new HeartBeatEvent(true));
        Assertions.assertFalse((Boolean) ReflectUtils.getFieldValue(service, "isRegistrationInProgress").orElse(false));
    }

    /**
     * Test the onInstanceChangedEvent method
     */
    @Test
    public void testOnInstanceChangedEvent() throws IllegalAccessException, NoSuchFieldException {
        // Clear data
        Field initField = GovernanceService.class.getDeclaredField("INIT");
        initField.setAccessible(true);
        AtomicBoolean INIT = (AtomicBoolean) initField.get(null);
        INIT.set(false);
        DubboCache.INSTANCE.setServiceName(null);

        // Test
        RegistryServiceImpl service = new RegistryServiceImpl();
        initGovernanceService(service);
        service.onInstanceChangedEvent(new InstanceChangedEvent("app", "default", Collections.emptyList()));
        Assertions.assertTrue(INIT.get());
    }

    private void mockServiceCenterClient(RegistryService service) {
        ServiceCenterClient client = Mockito.mock(ServiceCenterClient.class);
        ReflectUtils.setFieldValue(service, "client", client);
        MicroservicesResponse microservicesResponse = new MicroservicesResponse();
        List<Microservice> services = new ArrayList<>();
        Microservice microservice = new Microservice("bar");
        microservice.setServiceId("foo");
        microservice.setAppId("app");
        microservice.setSchemas(
            Arrays.asList("io.sermant.foo.FooTest", "io.sermant.foo.BarTest", "io.sermant.foo.Test"));
        services.add(microservice);
        microservicesResponse.setServices(services);
        Mockito.when(client.getMicroserviceList()).thenReturn(microservicesResponse);

        ReflectUtils.setFieldValue(service, "microservice", microservice);

        MicroserviceInstancesResponse instancesResponse = new MicroserviceInstancesResponse();
        List<MicroserviceInstance> instances = new ArrayList<>();
        MicroserviceInstance instance = new MicroserviceInstance();
        instance.setEndpoints(Collections.singletonList("dubbo://7.250.137.12:28821"));
        Map<String, String> properties = new HashMap<>();
        properties.put("dubbo.interface.data",
            "{\"io.sermant.foo.FooTest\":[{\"protocol\":[\"dubbo\"],\"version\":\"0.0.1\"}],\"io.sermant.foo.BarTest\":[{\"group\":\"bar2\",\"protocol\":[\"dubbo\"]},{\"protocol\":[\"dubbo\"]}]}");
        properties.put("version", "1.0.0");
        properties.put("tag", "tag1");
        instance.setProperties(properties);
        instances.add(instance);
        instancesResponse.setInstances(instances);
        Mockito.when(client.getMicroserviceInstanceList(Mockito.any())).thenReturn(instancesResponse);

        List<SchemaInfo> infos = new ArrayList<>();
        infos.add(new SchemaInfo("io.sermant.foo.BarTest",
            "dubbo-provider:48821/io.sermant.foo.BarTest?application=dubbo-provider&interface=io.sermant.foo.BarTest",
            ""));
        infos.add(new SchemaInfo("io.sermant.foo.FooTest",
            "dubbo-provider:48821/io.sermant.foo.FooTest?application=dubbo-provider&interface=io.sermant.foo.FooTest",
            ""));
        infos.add(new SchemaInfo("io.sermant.foo.Test",
            "dubbo-provider:48821/io.sermant.foo.Test?application=dubbo-provider&interface=io.sermant.foo.Test",
            ""));
        Mockito.when(client.getServiceSchemasList(Mockito.any(), Mockito.anyBoolean())).thenReturn(infos);
    }

    private void mockServiceCenterDiscovery(RegistryService service) {
        ServiceCenterDiscovery serviceCenterDiscovery = Mockito.mock(ServiceCenterDiscovery.class);
        ReflectUtils.setFieldValue(service, "serviceCenterDiscovery", serviceCenterDiscovery);
    }

    private void initGovernanceService(RegistryService service) {
        ReflectUtils.setFieldValue(service, "governanceService", new GovernanceService());
    }

    private void init(RegistryService service) {
        mockServiceCenterClient(service);
        mockServiceCenterDiscovery(service);
        initGovernanceService(service);
    }

    private <T> T getFieldValue(Object obj, String fieldName, Class<T> clazz) {
        return clazz.cast(ReflectUtils.getFieldValue(obj, fieldName).orElse(null));
    }

    /**
     * NotifyListener Test class
     *
     * @since 2022-02-09
     */
    public static class TestNotifyListener implements NotifyListener {
        private List<URL> list;

        @Override
        public void notify(List<URL> urls) {
            this.list = urls;
        }

        public List<URL> getList() {
            return list;
        }
    }
}