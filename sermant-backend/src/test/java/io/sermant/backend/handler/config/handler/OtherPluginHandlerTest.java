package io.sermant.backend.handler.config.handler;

import io.sermant.backend.entity.config.ConfigInfo;
import io.sermant.backend.entity.config.PluginType;
import io.sermant.backend.handler.config.OtherPluginHandler;
import io.sermant.backend.handler.config.PluginConfigHandler;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class OtherPluginHandlerTest {
    private static final String DEFAULT_KEY = "sermant.database.write.provider";

    private static final String DEFAULT_GROUP = "app=default&environment=prod&zone=gz";

    @Test
    public void parsePluginInfo() {
        PluginConfigHandler handler = new OtherPluginHandler();
        ConfigInfo configInfo = handler.parsePluginInfo(DEFAULT_KEY, DEFAULT_GROUP);
        Assert.assertEquals(configInfo.getKey(), DEFAULT_KEY);
        Assert.assertEquals(configInfo.getGroup(), DEFAULT_GROUP);
        Assert.assertEquals(configInfo.getPluginType(), PluginType.OTHER.getPluginName());
    }

    @Test
    public void verifyConfiguration() {
        PluginConfigHandler handler = new OtherPluginHandler();
        Assert.assertTrue(handler.verifyConfiguration(DEFAULT_KEY, DEFAULT_GROUP));
    }
}