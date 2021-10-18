package com.huawei.apm.premain.lubanops.agent;

import static com.huawei.apm.bootstrap.lubanops.commons.LubanApmConstants.APP_NAME_COMMONS;
import static com.huawei.apm.bootstrap.lubanops.commons.LubanApmConstants.BIZ_PATH_COMMONS;
import static com.huawei.apm.bootstrap.lubanops.commons.LubanApmConstants.CONFIG_FILENAME;
import static com.huawei.apm.bootstrap.lubanops.commons.LubanApmConstants.ENV_COMMONS;
import static com.huawei.apm.bootstrap.lubanops.commons.LubanApmConstants.ENV_SECRET_COMMONS;
import static com.huawei.apm.bootstrap.lubanops.commons.LubanApmConstants.ENV_TAG_COMMONS;
import static com.huawei.apm.bootstrap.lubanops.commons.LubanApmConstants.INSTANCE_NAME_COMMONS;
import static com.huawei.apm.bootstrap.lubanops.commons.LubanApmConstants.SUB_BUSINESS_COMMONS;

import com.huawei.apm.bootstrap.lubanops.commons.ValidatorUtil;
import com.huawei.apm.bootstrap.lubanops.utils.FileUtils;
import com.huawei.apm.bootstrap.lubanops.utils.StringUtils;
import com.huawei.apm.premain.AgentPremain;
import com.huawei.apm.premain.lubanops.utils.LibPathUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author
 * @date 2021/2/5 9:26
 */
public class ArgumentBuilder {

    public final static String PARAM_CHECKER_DEFAULT = "^[A-Za-z][A-Za-z0-9_-]{0,127}$";

    public final static String PARAM_CHECKER_SUB_BIZ
        = "^[A-Za-z][A-Za-z0-9_-]{0,127}(/[A-Za-z][A-Za-z0-9_-]{0,127}){0,2}$";

    public final static int PARAM_CHECKER_LENGTH_DEFAULT = 128;


    public static final String DEFAULT_INSTANCE = "default";

    public final static String APP_NAME = "app.name";

    public final static String INSTANCE_NAME = "instance.name";

    public final static String ENV = "env";

    public final static String ENV_TAG = "env.tag";

    public final static String BIZ_PATH = "business";

    public final static String SUB_BUSINESS = "sub.business";

    public final static String ENV_SECRET = "env.secret";

    /**
     * 应用名称(必填)
     */
    private String appName;

    /**
     * 实例名称
     */
    private String instanceName;

    /**
     * 环境名称
     */
    private String env;

    /**
     * 环境标签
     */
    private String envTag;

    /**
     * 业务路径
     */
    private String bizPath;

    /**
     * 子业务路径
     */
    private String subBusiness;

    /**
     * 实例id
     */
    private String envSecret;

    public Map build(String agentArgs, AgentPremain.LogInitCallback callback) {
        agentArgs = StringUtils.isBlank(agentArgs) ? "" : agentArgs.trim();

        buildEargerArgs(agentArgs, callback);

        buildFromAgentArg(agentArgs);

        buildFromConfigFile();

        buildFromEnvironment();

        checkArgs();

        if (StringUtils.isBlank(instanceName)) {
            instanceName = DEFAULT_INSTANCE;
        }
        return buildArgsMap();
    }

    private void buildEargerArgs(String agentArgs, AgentPremain.LogInitCallback callback) {
        if (!StringUtils.isBlank(agentArgs)) {
            String[] argArr = agentArgs.split(",");
            for (String arg : argArr) {
                String[] pair = arg.split("=");
                if (APP_NAME_COMMONS.equals(pair[0])) {
                    appName = pair[1];
                }
                if (INSTANCE_NAME_COMMONS.equals(pair[0])) {
                    instanceName = pair[1];
                }
            }
        }
        Properties properties = FileUtils.readFilePropertyByPath(
            LibPathUtils.getAgentPath() + File.separator + CONFIG_FILENAME);
        if (StringUtils.isBlank(appName)) {
            appName = properties.getProperty(APP_NAME);
        }
        if (StringUtils.isBlank(instanceName)) {
            instanceName = properties.getProperty(INSTANCE_NAME);
        }
        if (StringUtils.isBlank(appName)) {
            appName = System.getProperty(APP_NAME_COMMONS);
        }
        if (StringUtils.isBlank(instanceName)) {
            instanceName = System.getProperty(INSTANCE_NAME_COMMONS);
        }

        if (StringUtils.isBlank(instanceName)) {
            instanceName = DEFAULT_INSTANCE;
        }

        ValidatorUtil.validate(APP_NAME, appName, true, PARAM_CHECKER_LENGTH_DEFAULT, PARAM_CHECKER_DEFAULT);
        ValidatorUtil.validate(INSTANCE_NAME, instanceName, false, PARAM_CHECKER_LENGTH_DEFAULT, PARAM_CHECKER_DEFAULT);

        callback.initLog(appName, instanceName);
    }

    private Map buildArgsMap() {
        Map argMap = new HashMap(32);
        argMap.put(APP_NAME_COMMONS, appName);
        argMap.put(INSTANCE_NAME_COMMONS, instanceName);
        argMap.put(ENV_COMMONS, env);
        argMap.put(ENV_TAG_COMMONS, envTag);
        argMap.put(BIZ_PATH_COMMONS, bizPath);
        argMap.put(SUB_BUSINESS_COMMONS, subBusiness);
        argMap.put(ENV_SECRET_COMMONS, envSecret);
        return argMap;
    }


    private void checkArgs() {
        ValidatorUtil.validate(ENV, env, false, PARAM_CHECKER_LENGTH_DEFAULT, PARAM_CHECKER_DEFAULT);
        ValidatorUtil.validate(ENV_TAG, envTag, false, PARAM_CHECKER_LENGTH_DEFAULT, PARAM_CHECKER_DEFAULT);
        ValidatorUtil.validate(BIZ_PATH, bizPath, false, PARAM_CHECKER_LENGTH_DEFAULT, PARAM_CHECKER_DEFAULT);
        ValidatorUtil.validate(SUB_BUSINESS, subBusiness, false, PARAM_CHECKER_LENGTH_DEFAULT, PARAM_CHECKER_SUB_BIZ);
        ValidatorUtil.validate(ENV_SECRET, envSecret, false, PARAM_CHECKER_LENGTH_DEFAULT, PARAM_CHECKER_DEFAULT);
    }

    private void buildFromEnvironment() {
        if (StringUtils.isBlank(env)) {
            env = System.getProperty(ENV_COMMONS);
        }
        if (StringUtils.isBlank(envTag)) {
            envTag = System.getProperty(ENV_TAG_COMMONS);
        }
        if (StringUtils.isBlank(bizPath)) {
            bizPath = System.getProperty(BIZ_PATH_COMMONS);
        }
        if (StringUtils.isBlank(subBusiness)) {
            subBusiness = System.getProperty(SUB_BUSINESS_COMMONS);
        }
        if (StringUtils.isBlank(envSecret)) {
            envSecret = System.getProperty(ENV_SECRET_COMMONS);
        }
    }

    private void buildFromConfigFile() {
        Properties properties = FileUtils.readFilePropertyByPath(
            LibPathUtils.getAgentPath() + File.separator + CONFIG_FILENAME);
        if (StringUtils.isBlank(env)) {
            env = properties.getProperty(ENV);
        }
        if (StringUtils.isBlank(envTag)) {
            envTag = properties.getProperty(ENV_TAG);
        }
        if (StringUtils.isBlank(bizPath)) {
            bizPath = properties.getProperty(BIZ_PATH);
        }
        if (StringUtils.isBlank(subBusiness)) {
            subBusiness = properties.getProperty(SUB_BUSINESS);
        }
        if (StringUtils.isBlank(envSecret)) {
            envSecret = properties.getProperty(ENV_SECRET);
        }
    }

    private void buildFromAgentArg(String agentArgs) {
        if (!StringUtils.isBlank(agentArgs)) {
            String[] argArr = agentArgs.split(",");
            for (String arg : argArr) {
                String[] pair = arg.split("=");
                if (ENV_COMMONS.equals(pair[0])) {
                    env = pair[1];
                }
                if (ENV_COMMONS.equals(pair[0])) {
                    env = pair[1];
                }
                if (ENV_TAG_COMMONS.equals(pair[0])) {
                    envTag = pair[1];
                }
                if (BIZ_PATH_COMMONS.equals(pair[0])) {
                    bizPath = pair[1];
                }
                if (SUB_BUSINESS_COMMONS.equals(pair[0])) {
                    subBusiness = pair[1];
                }
                if (ENV_SECRET_COMMONS.equals(pair[0])) {
                    envSecret = pair[1];
                }
            }
        }
    }

}
