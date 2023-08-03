package com.huaweicloud.sermant.premain.common;

/**
 * 启动所需公共常量
 *
 * @author luanwenfei
 * @since 2023-07-20
 */
public class BootConstant {
    /**
     * 启动配置文件名
     */
    public static final String BOOTSTRAP_CONFIG_FILE_NAME = "bootstrap.properties";

    /**
     * 核心配置文件名
     */
    public static final String CORE_CONFIG_FILE_NAME = "config.properties";

    /**
     * 插件设定文件名
     */
    public static final String PLUGIN_SETTING_FILE_NAME = "plugins.yaml";

    /**
     * logback配置文件名
     */
    public static final String LOG_SETTING_FILE_NAME = "logback.xml";

    /**
     * 启动配置中制品名的键
     */
    public static final String ARTIFACT_NAME_KEY = "artifact";

    /**
     * 启动配置中应用名称的键
     */
    public static final String APP_NAME_KEY = "appName";

    /**
     * 启动配置中应用类型的键
     */
    public static final String APP_TYPE_KEY = "appType";

    /**
     * 启动配置中服务名称的键
     */
    public static final String SERVICE_NAME_KEY = "serviceName";

    /**
     * 启动配置中的agentPath
     */
    public static final String AGENT_ROOT_DIR_KEY = "agentPath";

    /**
     * Sermant核心功能实现包的键
     */
    public static final String CORE_IMPLEMENT_DIR_KEY = "core.implement.dir";

    /**
     * Sermant公共第三方依赖目录
     */
    public static final String COMMON_DEPENDENCY_DIR_KEY = "common.dir";

    /**
     * sermant的配置文件名的键
     */
    public static final String CORE_CONFIG_FILE_KEY = "core.config.file";

    /**
     * 插件的设置配置名的键
     */
    public static final String PLUGIN_SETTING_FILE_KEY = "plugin.setting.file";

    /**
     * pluginPackage插件包的键
     */
    public static final String PLUGIN_PACKAGE_DIR_KEY = "plugin.package.dir";

    /**
     * logback配置的键
     */
    public static final String LOG_SETTING_FILE_KEY = "log.setting.file";
}
