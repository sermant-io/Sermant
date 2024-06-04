package io.sermant.backend.handler.config.handler;

import io.sermant.backend.entity.config.ConfigInfo;
import io.sermant.backend.entity.config.PluginType;
import io.sermant.backend.handler.config.FlowControlPluginHandler;
import io.sermant.backend.handler.config.PluginConfigHandler;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FlowControlPluginHandlerTest {
    private static final String DEFAULT_SERVICE_NAME = "provider";

    private static final String DEFAULT_GROUP = "service=provider";

    private static final String ERROR_GROUP = "service=provider&app=default";

    private static final String ERROR_KEY = "testKey";

    private static final String RATE_LIMIT_CONFIGURATION_NAME = "servicecomb.rateLimiting.test";

    private static final String BULKHEAD_CONFIGURATION_NAME = "servicecomb.bulkhead.test";

    private static final String MATCH_GROUP_CONFIGURATION_NAME = "servicecomb.matchGroup.test";

    private static final String CIRCUIT_BREAKER_CONFIGURATION_NAME = "servicecomb.circuitBreaker.bulkhead";

    private static final String FAULT_INJECTION_CONFIGURATION_NAME = "servicecomb.faultInjection.test";

    private static final String RETRY_CONFIGURATION_NAME = "servicecomb.retry.test";

    private static final String SYSTEM_CONFIGURATION_NAME = "servicecomb.system.bulkhead";

    @Test
    public void parsePluginInfo() {
        PluginConfigHandler handler = new FlowControlPluginHandler();
        ConfigInfo configInfo = handler.parsePluginInfo(RATE_LIMIT_CONFIGURATION_NAME, DEFAULT_GROUP);
        Assert.assertEquals(configInfo.getServiceName(), DEFAULT_SERVICE_NAME);
        Assert.assertEquals(configInfo.getKey(), RATE_LIMIT_CONFIGURATION_NAME);
        Assert.assertEquals(configInfo.getGroup(), DEFAULT_GROUP);
        Assert.assertEquals(configInfo.getPluginType(), PluginType.FLOW_CONTROL.getPluginName());
    }

    @Test
    public void verifyConfiguration() {
        PluginConfigHandler handler = new FlowControlPluginHandler();
        Assert.assertTrue(handler.verifyConfiguration(RATE_LIMIT_CONFIGURATION_NAME, DEFAULT_GROUP));
        Assert.assertTrue(handler.verifyConfiguration(BULKHEAD_CONFIGURATION_NAME, DEFAULT_GROUP));
        Assert.assertTrue(handler.verifyConfiguration(MATCH_GROUP_CONFIGURATION_NAME, DEFAULT_GROUP));
        Assert.assertTrue(handler.verifyConfiguration(CIRCUIT_BREAKER_CONFIGURATION_NAME, DEFAULT_GROUP));
        Assert.assertTrue(handler.verifyConfiguration(FAULT_INJECTION_CONFIGURATION_NAME, DEFAULT_GROUP));
        Assert.assertTrue(handler.verifyConfiguration(RETRY_CONFIGURATION_NAME, DEFAULT_GROUP));
        Assert.assertTrue(handler.verifyConfiguration(SYSTEM_CONFIGURATION_NAME, DEFAULT_GROUP));
        Assert.assertFalse(handler.verifyConfiguration(RATE_LIMIT_CONFIGURATION_NAME, ERROR_GROUP));
        Assert.assertFalse(handler.verifyConfiguration(ERROR_KEY, DEFAULT_GROUP));
    }
}