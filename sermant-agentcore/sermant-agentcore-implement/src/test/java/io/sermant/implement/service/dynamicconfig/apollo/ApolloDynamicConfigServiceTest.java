/*
 * Copyright (C) 2025-2025 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.implement.service.dynamicconfig.apollo;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.client.exception.ApolloOpenApiException;
import com.ctrip.framework.apollo.openapi.dto.OpenAppNamespaceDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import io.sermant.core.config.ConfigManager;
import io.sermant.core.plugin.config.ServiceMeta;
import io.sermant.core.service.ServiceManager;
import io.sermant.core.service.dynamicconfig.config.DynamicConfig;
import io.sermant.implement.service.dynamicconfig.apollo.config.ApolloProviderManager;
import io.sermant.implement.service.dynamicconfig.apollo.config.ApolloMetaServerProvider;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author Chen Zhenyang
 * @since 2025-08-13
 */
@RunWith(MockitoJUnitRunner.class)
public class ApolloDynamicConfigServiceTest {
    public static final long SLEEP_TIME_MILLIS = 1000L;
    public final DynamicConfig dynamicConfig = new DynamicConfig();
    public final ServiceMeta serviceMeta = new ServiceMeta();
    private static final String APP_ID = "test-app";
    private static final String NAMESPACE = "application";
    private static boolean createAppNamespaceCalledFlag = false;

    public MockedStatic<ConfigManager> dynamicConfigMockedStatic;
    public MockedStatic<ServiceManager> serviceManagerMockedStatic;
    public MockedStatic<ConfigService> configServiceMockedStatic;
    public ApolloDynamicConfigService apolloDynamicConfigService;
    public MockedStatic<ApolloOpenApiClient> apolloApiClientMockedStatic;

    @Mock
    private ApolloOpenApiClient openApiClient;

    @Mock
    private Config config;

    // mock data structure
    private final Map<String, String> configStore = new HashMap<>();
    private final Set<ConfigChangeListener> activeListeners = new HashSet<>();
    private final Map<String, ConfigChange> stagedChanges = new HashMap<>();
    public ApolloOpenApiClient.ApolloOpenApiClientBuilder mockedBuilder;

    @Before
    public void setUp() throws Exception {
        dynamicConfig.setEnableAuth(false);
        dynamicConfig.setServerAddress("http://127.0.0.1:8080");
        dynamicConfig.setTimeoutValue(30000);
        dynamicConfig.setUserName("apollo");
        dynamicConfig.setDefaultGroup("application");
        dynamicConfig.setPrivateKey("c8aca3c749d84e759fd5872057aec199");

        serviceMeta.setApplication("apptest");
        serviceMeta.setEnvironment("DEV");
        Map<String, String> map = new HashMap<>();
        map.put("cluster", "default");
        map.put("adminUrl", "http://localhost:8070");
        map.put("token", "00f117c5d4b4c8719c84dbf03182261b68528d61bf8cf6014a6d21ed71717a3e");
        map.put("isDoubleCheck", Boolean.toString(false));
        serviceMeta.setParameters(map);

        dynamicConfigMockedStatic = mockStatic(ConfigManager.class);
        dynamicConfigMockedStatic.when(() -> ConfigManager.getConfig(DynamicConfig.class))
                .thenReturn(dynamicConfig);
        dynamicConfigMockedStatic.when(() -> ConfigManager.getConfig(ServiceMeta.class))
                .thenReturn(serviceMeta);

        configStore.clear();
        stagedChanges.clear();
        activeListeners.clear();

        configServiceMockedStatic = Mockito.mockStatic(ConfigService.class);
        configServiceMockedStatic.when(() -> ConfigService.getConfig(anyString()))
                .thenReturn(config);

        when(config.getProperty(anyString(), anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            String defaultValue = invocation.getArgument(1);
            return configStore.getOrDefault(key, defaultValue);
        });

        doAnswer(invocation -> {
            ConfigChangeListener listener = invocation.getArgument(0);
            activeListeners.add(listener);
            return null;
        }).when(config).addChangeListener(any(ConfigChangeListener.class), anySet());

        lenient().doAnswer(invocation -> {
            ConfigChangeListener listener = invocation.getArgument(0);
            activeListeners.add(listener);
            return null;
        }).when(config).addChangeListener(any(ConfigChangeListener.class));

        when(config.removeChangeListener(any(ConfigChangeListener.class))).thenAnswer(invocation -> {
            ConfigChangeListener listenerToRemove = invocation.getArgument(0);
            return activeListeners.remove(listenerToRemove);
        });

        lenient().doAnswer(invocation -> {
            OpenItemDTO item = invocation.getArgument(4);
            String key = item.getKey();
            String newValue = item.getValue();
            String oldValue = configStore.get(key);
            configStore.put(key, newValue);
            ConfigChange configChange;
            if (oldValue == null || oldValue.isEmpty())
                configChange = new ConfigChange(APP_ID, NAMESPACE, key, oldValue, newValue, PropertyChangeType.ADDED);
            else
                configChange = new ConfigChange(APP_ID, NAMESPACE, key, oldValue, newValue, PropertyChangeType.MODIFIED);
            stagedChanges.put(key, configChange);
            return null;
        }).when(openApiClient).createOrUpdateItem(anyString(), anyString(), anyString(), anyString(), any(OpenItemDTO.class));

        doAnswer(invocation -> {
            String key = invocation.getArgument(4);

            String oldValue = configStore.get(key);
            configStore.remove(key);

            if (oldValue != null) {
                ConfigChange configChange = new ConfigChange(APP_ID, NAMESPACE, key, oldValue, null, PropertyChangeType.DELETED);
                stagedChanges.put(key, configChange);
            }
            return null;
        }).when(openApiClient).removeItem(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());

        doAnswer(invocation -> {
            if (!stagedChanges.isEmpty() && !activeListeners.isEmpty()) {
                ConfigChangeEvent changeEvent = new ConfigChangeEvent(APP_ID, NAMESPACE, new HashMap<>(stagedChanges));
                for (ConfigChangeListener listener : activeListeners) {
                    listener.onChange(changeEvent);
                }
            }
            stagedChanges.clear();
            return null;
        }).when(openApiClient).publishNamespace(anyString(), anyString(), anyString(), anyString(), any());

        when(openApiClient.getNamespace(anyString(), anyString(), anyString(), anyString(), anyBoolean())).thenAnswer(invocation -> {
            if (!createAppNamespaceCalledFlag) {
                ApolloOpenApiException cause = new ApolloOpenApiException(
                        HttpURLConnection.HTTP_NOT_FOUND,
                        "Not Found",
                        "Mocked: Namespace not found"
                );
                throw new RuntimeException(cause);
            }
            String appId = invocation.getArgument(0);
            String cluster = invocation.getArgument(2);
            String namespace = invocation.getArgument(3);

            OpenNamespaceDTO namespaceDTO = new OpenNamespaceDTO();
            namespaceDTO.setAppId(appId);
            namespaceDTO.setClusterName(cluster);
            namespaceDTO.setNamespaceName(namespace);

            List<OpenItemDTO> items = new ArrayList<>();
            for (Map.Entry<String, String> entry : configStore.entrySet()) {
                OpenItemDTO itemDTO = new OpenItemDTO();
                itemDTO.setKey(entry.getKey());
                itemDTO.setValue(entry.getValue());
                itemDTO.setComment("mocked comment");
                items.add(itemDTO);
            }
            items.sort(Comparator.comparing(OpenItemDTO::getKey));
            namespaceDTO.setItems(items);
            return namespaceDTO;
        });
        when(openApiClient.getNamespace(anyString(), anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            if (!createAppNamespaceCalledFlag) {
                ApolloOpenApiException cause = new ApolloOpenApiException(
                        HttpURLConnection.HTTP_NOT_FOUND,
                        "Not Found",
                        "Mocked: Namespace not found"
                );
                throw new RuntimeException(cause);
            }
            String appId = invocation.getArgument(0);
            String cluster = invocation.getArgument(2);
            String namespace = invocation.getArgument(3);

            OpenNamespaceDTO namespaceDTO = new OpenNamespaceDTO();
            namespaceDTO.setAppId(appId);
            namespaceDTO.setClusterName(cluster);
            namespaceDTO.setNamespaceName(namespace);

            List<OpenItemDTO> items = new ArrayList<>();
            for (Map.Entry<String, String> entry : configStore.entrySet()) {
                OpenItemDTO itemDTO = new OpenItemDTO();
                itemDTO.setKey(entry.getKey());
                itemDTO.setValue(entry.getValue());
                itemDTO.setComment("mocked comment");
                items.add(itemDTO);
            }
            namespaceDTO.setItems(items);
            return namespaceDTO;
        });
        when(openApiClient.createAppNamespace(any(OpenAppNamespaceDTO.class))).thenAnswer(invocation -> {
            createAppNamespaceCalledFlag = true;
            OpenAppNamespaceDTO inputDto = invocation.getArgument(0);
            return inputDto;
        });
        // mock build Apollo open api client
        apolloApiClientMockedStatic = Mockito.mockStatic(ApolloOpenApiClient.class);
        mockedBuilder = mock(ApolloOpenApiClient.ApolloOpenApiClientBuilder.class);
        apolloApiClientMockedStatic.when(ApolloOpenApiClient::newBuilder).thenReturn(mockedBuilder);
        lenient().when(mockedBuilder.withPortalUrl(anyString())).thenReturn(mockedBuilder);
        lenient().when(mockedBuilder.withToken(anyString())).thenReturn(mockedBuilder);
        lenient().when(mockedBuilder.withConnectTimeout(anyInt())).thenReturn(mockedBuilder);
        lenient().when(mockedBuilder.withReadTimeout(anyInt())).thenReturn(mockedBuilder);
        when(mockedBuilder.build()).thenReturn(this.openApiClient);
        apolloDynamicConfigService = new ApolloDynamicConfigService();
        apolloDynamicConfigService.start();
        serviceManagerMockedStatic = mockStatic(ServiceManager.class);
        serviceManagerMockedStatic.when(() -> ServiceManager.getService(ApolloDynamicConfigService.class))
                .thenReturn(apolloDynamicConfigService);
    }

    @After
    public void stop() {
        if (apolloDynamicConfigService != null) {
            apolloDynamicConfigService.stop();
        }
        if (dynamicConfigMockedStatic != null) {
            dynamicConfigMockedStatic.close();
        }
        if (serviceManagerMockedStatic != null) {
            serviceManagerMockedStatic.close();
        }
        if (configServiceMockedStatic != null) {
            configServiceMockedStatic.close();
        }
        if (apolloApiClientMockedStatic != null) {
            apolloApiClientMockedStatic.close();
        }
    }

    @Test
    public void testApolloDynamicConfigService() throws Exception {
        try {
            // Config class tests
            ApolloProviderManager apolloProviderManager = new ApolloProviderManager();
            apolloProviderManager.initialize();
            Assert.assertEquals(serviceMeta.getApplication(), apolloProviderManager.getProperty("app.id", ""));
            Assert.assertEquals(serviceMeta.getEnvironment(), apolloProviderManager.getProperty("env", ""));
            Assert.assertEquals(serviceMeta.getParameters().get("cluster"), apolloProviderManager.getProperty("apollo.cluster", ""));
            Assert.assertEquals(dynamicConfig.getPrivateKey(), apolloProviderManager.getProperty("apollo.access-key.secret", ""));
            ApolloMetaServerProvider apolloMetaServerProvider = new ApolloMetaServerProvider();
            Assert.assertEquals(dynamicConfig.getServerAddress(), apolloMetaServerProvider.getMetaServerAddress(Env.DEV));
            ApolloClient apolloClientSpy = Mockito.spy(new ApolloClient());
            doReturn(HttpURLConnection.HTTP_OK).when(apolloClientSpy).getResponse(any(HttpURLConnection.class));
            Assert.assertTrue(apolloClientSpy.isConnect());
            doReturn(HttpURLConnection.HTTP_BAD_GATEWAY).when(apolloClientSpy).getResponse(any(HttpURLConnection.class));
            Assert.assertFalse(apolloClientSpy.isConnect());

            // Test publish and get configuration: valid group name
            Assert.assertTrue(
                    apolloDynamicConfigService.doPublishConfig("testTrueSingleConfigKey",
                            "test.True_Single_Config-Group",
                            "testTrueSingleConfigContent"));
            apolloDynamicConfigService.doPublishConfig("testTrueSingleConfigKey2",
                    "test.True_Single_Config-Group",
                    "testTrueSingleConfigContent2");

            Assert.assertEquals("testTrueSingleConfigContent", apolloDynamicConfigService
                    .doGetConfig("testTrueSingleConfigKey", "test.True_Single_Config-Group").orElse(""));

            // Test publish and get configuration: invalid group name
            Assert.assertFalse(
                    apolloDynamicConfigService.doPublishConfig("testErrorSingleConfigKey",
                            "test+++Error&Single" + ":Config-Group", "testErrorSingleConfigContent"));
            Thread.sleep(SLEEP_TIME_MILLIS);
            Assert.assertEquals(Optional.empty(), apolloDynamicConfigService.doGetConfig(
                    "testErrorSingleConfigKey", "test+++.Error_Single:Config-Group"));

            List<String> list = new ArrayList<>();
            list.add("testTrueSingleConfigKey");
            list.add("testTrueSingleConfigKey2");
            Assert.assertEquals(list.toString(),
                    apolloDynamicConfigService.doListKeysFromGroup("test.True_Single_Config-Group").toString());

            // Test remove configuration
            Assert.assertTrue(apolloDynamicConfigService.doRemoveConfig("testTrueSingleConfigKey",
                    "test.True_Single_Config-Group"));
            apolloDynamicConfigService.doRemoveConfig("testTrueSingleConfigKey2", "test.True_Single_Config-Group");
            Thread.sleep(SLEEP_TIME_MILLIS);
            Assert.assertEquals("",
                    apolloDynamicConfigService.doGetConfig("testTrueSingleConfigKey",
                            "test.True_Single_Config-Group").orElse(""));

            // Test listener addition
            ApolloTestListener testListener = new ApolloTestListener();
            Assert.assertTrue(
                    apolloDynamicConfigService.doPublishConfig("testSingleListenerKey", "testSingleListenerGroup",
                            "testSingleListenerContent"));
            Assert.assertTrue(
                    apolloDynamicConfigService.doAddConfigListener("testSingleListenerKey", "testSingleListenerGroup",
                            testListener));
            Thread.sleep(SLEEP_TIME_MILLIS);
            Assert.assertTrue(
                    apolloDynamicConfigService.doPublishConfig("testSingleListenerKey", "testSingleListenerGroup",
                            "testSingleListenerContent-3"));

            checkChangeTrue(testListener, "testSingleListenerContent-3");

            // Test listener removal
            Assert.assertTrue(apolloDynamicConfigService.doRemoveConfigListener("testSingleListenerKey",
                    "testSingleListenerGroup"));
            Assert.assertTrue(
                    apolloDynamicConfigService.doPublishConfig("testSingleListenerKey", "testSingleListenerGroup",
                            "testSingleListenerContent-3"));

            checkChangeFalse(testListener);
            Assert.assertTrue(
                    apolloDynamicConfigService.doRemoveConfig("testSingleListenerKey", "testSingleListenerGroup"));

            // Test the addition of group listeners and get all keys in the group
            ApolloTestListener testListenerGroup = new ApolloTestListener();
            Assert.assertTrue(
                    apolloDynamicConfigService.doPublishConfig("testGroupListenerKey-1", "testGroupListenerGroup",
                            "testGroupListenerContent-1"));
            Assert.assertTrue(
                    apolloDynamicConfigService.doPublishConfig("testGroupListenerKey-2", "testGroupListenerGroup",
                            "testGroupListenerContent-2"));
            Assert.assertTrue(apolloDynamicConfigService.doAddGroupListener("testGroupListenerGroup",
                    testListenerGroup));
            Thread.sleep(SLEEP_TIME_MILLIS);
            Assert.assertTrue(
                    apolloDynamicConfigService.doPublishConfig("testGroupListenerKey-2", "testGroupListenerGroup",
                            "testGroupListenerContent-2-2"));
            Assert.assertEquals(2, apolloDynamicConfigService.doListKeysFromGroup("testGroupListenerGroup").size());

            checkChangeTrue(testListenerGroup, "testGroupListenerContent-2-2");

            // Test removal of group listeners
            Assert.assertTrue(apolloDynamicConfigService.doRemoveGroupListener("testGroupListenerGroup"));
            Assert.assertTrue(
                    apolloDynamicConfigService.doPublishConfig("testGroupListenerKey-3", "testGroupListenerGroup",
                            "testGroupListenerKey-3"));

            checkChangeFalse(testListenerGroup);
            Assert.assertTrue(
                    apolloDynamicConfigService.doRemoveConfig("testGroupListenerKey-1", "testGroupListenerGroup"));
            Assert.assertTrue(
                    apolloDynamicConfigService.doRemoveConfig("testGroupListenerKey-2", "testGroupListenerGroup"));
            Assert.assertTrue(
                    apolloDynamicConfigService.doRemoveConfig("testGroupListenerKey-3", "testGroupListenerGroup"));
        } finally {
            apolloDynamicConfigService.doRemoveConfig("testTrueSingleConfigKey", "test.True_Single:Config-Group");
            apolloDynamicConfigService.doRemoveConfig("testSingleListenerKey", "testSingleListenerGroup");
            apolloDynamicConfigService.doRemoveConfig("testGroupListenerKey-1", "testGroupListenerGroup");
            apolloDynamicConfigService.doRemoveConfig("testGroupListenerKey-2", "testGroupListenerGroup");
            apolloDynamicConfigService.doRemoveConfig("testGroupListenerKey-3", "testGroupListenerGroup");
        }
    }

    public void checkChangeTrue(ApolloTestListener testListener, String predictContent) throws InterruptedException {
        Thread.sleep(SLEEP_TIME_MILLIS);
        Assert.assertTrue(testListener.isChange());
        Assert.assertEquals(predictContent, testListener.getContent());
        testListener.setChange(false);
    }

    public void checkChangeFalse(ApolloTestListener testListener) throws InterruptedException {
        Thread.sleep(SLEEP_TIME_MILLIS);
        Assert.assertFalse(testListener.isChange());
        testListener.setChange(false);
    }
}

class ApolloTestListener implements DynamicConfigListener {
    /**
     * Listening success flag
     */
    private boolean isChange = false;

    /**
     * Listened configuration content
     */
    private String content;

    @Override
    public void process(DynamicConfigEvent event) {
        setContent(event.getContent());
        setChange(true);
    }

    public boolean isChange() {
        return isChange;
    }

    public void setChange(boolean change) {
        isChange = change;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
