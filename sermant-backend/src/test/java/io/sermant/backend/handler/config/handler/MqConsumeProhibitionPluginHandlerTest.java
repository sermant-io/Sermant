package io.sermant.backend.handler.config.handler;

import io.sermant.backend.entity.config.ConfigInfo;
import io.sermant.backend.entity.config.PluginType;
import io.sermant.backend.handler.config.MqConsumeProhibitionPluginHandler;
import io.sermant.backend.handler.config.PluginConfigHandler;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MqConsumeProhibitionPluginHandlerTest {
    private static final String DEFAULT_APP_NAME = "default";

    private static final String DEFAULT_ENVIRONMENT_NAME = "prod";

    private static final String DEFAULT_ZONE_NAME = "gz";

    private static final String DEFAULT_SERVICE_NAME = "provider";

    private static final String DEFAULT_GROUP = "app=default&environment=prod&zone=gz";

    private static final String ERROR_GROUP = "app=default&env=prod&zone=gz";

    private static final String ERROR_KEY = "testKey";

    private static final String SERVICE_CONFIGURATION_NAME = "sermant.mq.consume.provider";

    private static final String GLOBAL_CONFIGURATION_NAME = "sermant.mq.consume.globalConfig";

    @Test
    public void parsePluginInfo() {
        PluginConfigHandler handler = new MqConsumeProhibitionPluginHandler();
        ConfigInfo configInfo = handler.parsePluginInfo(SERVICE_CONFIGURATION_NAME, DEFAULT_GROUP);
        Assert.assertEquals(configInfo.getAppName(), DEFAULT_APP_NAME);
        Assert.assertEquals(configInfo.getEnvironment(), DEFAULT_ENVIRONMENT_NAME);
        Assert.assertEquals(configInfo.getZone(), DEFAULT_ZONE_NAME);
        Assert.assertEquals(configInfo.getServiceName(), DEFAULT_SERVICE_NAME);
        Assert.assertEquals(configInfo.getKey(), SERVICE_CONFIGURATION_NAME);
        Assert.assertEquals(configInfo.getGroup(), DEFAULT_GROUP);
        Assert.assertEquals(configInfo.getPluginType(), PluginType.MQ_CONSUME_PROHIBITION.getPluginName());
    }

    @Test
    public void verifyConfiguration() {
        PluginConfigHandler handler = new MqConsumeProhibitionPluginHandler();
        Assert.assertTrue(handler.verifyConfiguration(SERVICE_CONFIGURATION_NAME, DEFAULT_GROUP));
        Assert.assertTrue(handler.verifyConfiguration(GLOBAL_CONFIGURATION_NAME, DEFAULT_GROUP));
        Assert.assertFalse(handler.verifyConfiguration(SERVICE_CONFIGURATION_NAME, ERROR_GROUP));
        Assert.assertFalse(handler.verifyConfiguration(ERROR_KEY, DEFAULT_GROUP));
    }
}