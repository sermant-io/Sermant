package io.sermant.backend.handler.config.handler;

import io.sermant.backend.entity.config.ConfigInfo;
import io.sermant.backend.entity.config.PluginType;
import io.sermant.backend.handler.config.PluginConfigHandler;
import io.sermant.backend.handler.config.TagTransmissionPluginHandler;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TagTransmissionPluginHandlerTest {
    private static final String CONFIGURATION_GROUP_NAME = "sermant/tag-transmission-plugin";

    private static final String CONFIGURATION_KEY_NAME = "tag-config";

    private static final String ERROR_KEY = "testKey";

    private static final String ERROR_GROUP = "app=default&env=prod&service=provider";

    @Test
    public void parsePluginInfo() {
        PluginConfigHandler handler = new TagTransmissionPluginHandler();
        ConfigInfo configInfo = handler.parsePluginInfo(CONFIGURATION_KEY_NAME, CONFIGURATION_GROUP_NAME);
        Assert.assertEquals(configInfo.getKey(), CONFIGURATION_KEY_NAME);
        Assert.assertEquals(configInfo.getGroup(), CONFIGURATION_GROUP_NAME);
        Assert.assertEquals(configInfo.getPluginType(), PluginType.TAG_TRANSMISSION.getPluginName());
    }

    @Test
    public void verifyConfiguration() {
        PluginConfigHandler handler = new TagTransmissionPluginHandler();
        Assert.assertTrue(handler.verifyConfiguration(CONFIGURATION_KEY_NAME, CONFIGURATION_GROUP_NAME));
        Assert.assertFalse(handler.verifyConfiguration(ERROR_KEY, CONFIGURATION_GROUP_NAME));
        Assert.assertFalse(handler.verifyConfiguration(CONFIGURATION_KEY_NAME, ERROR_GROUP));
    }
}