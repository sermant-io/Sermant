package com.lubanops.apm.core.config;

import com.lubanops.apm.core.utils.AgentPath;

/**
 * 腳本中的配置信息<br>
 *
 * @author
 * @since 2020年3月16日
 */
public class StartupConfigManager {

    public final static String APP_NAME_KEY = "luban_apm_appName";

    public final static String INSTANCE_NAME_KEY = "luban_apm_instanceName";

    public final static String ENV_KEY = "luban_apm_env";

    public final static String ENV_TAG_KEY = "luban_apm_env_tag";

    public final static String BIZ_PATH_KEY = "luban_apm_bizpath";

    public final static String ENV_SECRET_KEY = "luban_apm_env_secret";

    /**
     * 应用名称(必填)
     */
    public String appName;

    /**
     * 实例名称
     */
    public String instanceName;

    /**
     * 环境名称
     */
    public String env;

    /**
     * 环境标签
     */
    public String envTag;

    /**
     * 业务路径
     */
    public String bizPath;

    public String agentPath;

    public StartupConfigManager() {

    }

    public static StartupConfigManager parse(String[] args) {
        StartupConfigManager configManager = new StartupConfigManager();
        for (String arg : args) {
            String[] pair = arg.split(",");
            if (APP_NAME_KEY.equals(pair[0])) {
                configManager.setAppName(pair[1]);
            }
            if (INSTANCE_NAME_KEY.equals(pair[0])) {
                configManager.setInstanceName(pair[1]);
            }
            if (ENV_KEY.equals(pair[0])) {
                configManager.setEnv(pair[1]);
            }
            if (ENV_TAG_KEY.equals(pair[0])) {
                configManager.setEnvTag(pair[1]);
            }
            if (BIZ_PATH_KEY.equals(pair[0])) {
                configManager.setBizPath(pair[1]);
            }
        }
        configManager.setAgentPath(AgentPath.getInstance().getAgentPath());
        return configManager;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getEnvTag() {
        return envTag;
    }

    public void setEnvTag(String envTag) {
        this.envTag = envTag;
    }

    public String getBizPath() {
        return bizPath;
    }

    public void setBizPath(String bizPath) {
        this.bizPath = bizPath;
    }

    public String getAgentPath() {
        return agentPath;
    }

    public void setAgentPath(String agentPath) {
        this.agentPath = agentPath;
    }
}
