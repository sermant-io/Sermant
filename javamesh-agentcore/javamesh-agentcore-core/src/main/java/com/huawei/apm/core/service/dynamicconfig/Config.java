package com.huawei.apm.core.service.dynamicconfig;

import com.huawei.apm.core.config.BaseConfig;
import com.huawei.apm.core.config.ConfigLoader;
import com.huawei.apm.core.config.ConfigTypeKey;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.service.dynamicconfig.service.DynamicConfigType;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * Config for this DynamicConfig Module
 *
 */
@ConfigTypeKey("dynamicconfig")
public class Config implements BaseConfig {

    private static final Logger logger = LogFactory.getLogger();

    static Config singleInst;

    public synchronized static Config getInstance()
    {
        if ( singleInst == null ) {
            Config config = ConfigLoader.getConfig(Config.class);
            singleInst = config;
        }
        if ( singleInst == null ) {
            logger.log(Level.WARNING, "Config failed to init from configfile. Load config from hardcode.");
            singleInst = new Config();
        }
        return singleInst;
    }

    public static int getTimeout_value() {
        return getInstance().timeout_value;
    }

    public static String getDefaultGroup() {
        return getInstance().default_group;
    }

    public static String getZookeeperUri() {
        return getInstance().zookeeper_uri;
    }

    public static DynamicConfigType getDynamicConfigType() {
        return getInstance().dynamicConfigType;
    }

    protected int timeout_value = 30000;

    protected String default_group = "java-mesh";

    protected String zookeeper_uri = "zookeeper://127.0.0.1:2181";

    protected DynamicConfigType dynamicConfigType = DynamicConfigType.ZooKeeper;

}
