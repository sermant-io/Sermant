package com.huawei.apm.core.lubanops.bootstrap.commons;

import java.nio.charset.Charset;

/**
 * @author
 * @date 2020/9/22 16:04
 */
public class LubanApmConstants {

    public final static String LUBANOPS_APM_PRODUCT_NAME = "apm";

    public final static Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    public final static int DEFAULT_SERVICE_SHUTDOWN_TIMEOUT = 1 * 1000;

    public final static int AGENT_BOOTSTRAP_REGISTER_RETRY_INTERVAL = 100;

    public final static int AGENT_BOOTSTRAP_REGISTER_RETRY_TIMES = 10;

    // ~~ data type
    public final static String SPAN_EVENT_DATA_TYPE = "SpanEventData";

    public final static String MONITOR_DATA_TYPE = "MonitorData";

    // ~~ config key
    public final static String CONFIG_FILENAME = "bootstrap.properties";

    public final static String APP_NAME_COMMONS = "appName";

    public final static String INSTANCE_NAME_COMMONS = "instanceName";

    public final static String ENV_COMMONS = "env";

    public final static String ENV_TAG_COMMONS = "envTag";

    public final static String BIZ_PATH_COMMONS = "business";

    public final static String SUB_BUSINESS_COMMONS = "subBusiness";

    public final static String ENV_SECRET_COMMONS = "envSecret";

    public final static String AGENT_PATH_COMMONS = "agentPath";

    public final static String BOOT_PATH_COMMONS = "bootPath";

    public final static String PLUGINS_PATH_COMMONS = "pluginsPath";

    public final static String APP_TYPE_COMMON = "appType";

    public final static String NETTY_SERVER_IP_COMMON = "127.0.0.1";

    public final static String NETTY_SERVER_PORT_COMMON = "6888";
}
