/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.javamesh.core.lubanops.bootstrap.config;

import static com.huawei.javamesh.core.lubanops.bootstrap.commons.LubanApmConstants.APP_NAME_COMMONS;
import static com.huawei.javamesh.core.lubanops.bootstrap.commons.LubanApmConstants.BIZ_PATH_COMMONS;
import static com.huawei.javamesh.core.lubanops.bootstrap.commons.LubanApmConstants.ENV_COMMONS;
import static com.huawei.javamesh.core.lubanops.bootstrap.commons.LubanApmConstants.ENV_SECRET_COMMONS;
import static com.huawei.javamesh.core.lubanops.bootstrap.commons.LubanApmConstants.ENV_TAG_COMMONS;
import static com.huawei.javamesh.core.lubanops.bootstrap.commons.LubanApmConstants.INSTANCE_NAME_COMMONS;
import static com.huawei.javamesh.core.lubanops.bootstrap.commons.LubanApmConstants.SUB_BUSINESS_COMMONS;
import static com.huawei.javamesh.core.lubanops.bootstrap.commons.LubanApmConstants.APP_TYPE_COMMON;

import com.huawei.javamesh.core.lubanops.bootstrap.utils.StringUtils;

import java.util.Map;

/**
 * 用户身份的配置信息<br>
 *
 * @author
 * @since 2020年3月9日
 */
public class IdentityConfigManager {

    public static final String DEFAULT_ENV = "default";

    public static final String DEFAULT_BUSINESS = "default";

    /**
     * 应用名称(必填)
     */
    private static String appName;

    /**
     * 应用类型
     */
    private static int appType;

    /**
     * 实例名称
     */
    private static String instanceName;

    /**
     * 环境名称
     */
    private static String env;

    /**
     * 环境标签
     */
    private static String envTag;

    /**
     * 业务路径
     */
    private static String bizPath;

    /**
     * 子业务路径
     */
    private static String subBusiness;

    /**
     * 环境的ID
     */
    private static long envId;

    /**
     * 应用的ID
     */
    private static long appId;

    /**
     * 业务ID
     */
    private static long bizId;

    /**
     * 租户的ID
     */
    private static int domainId;

    /**
     * 实例id
     */
    private static long instanceId;

    /**
     * 实例id
     */
    private static String envSecret;

    private static Map<String, String> attachment;

    public static void init(Map argsMap, String configPath) {
        appName = argsMap.get(APP_NAME_COMMONS).toString();
        instanceName = argsMap.get(INSTANCE_NAME_COMMONS).toString();
        env = argsMap.get(ENV_COMMONS) == null ? "" : argsMap.get(ENV_COMMONS).toString();
        envTag = argsMap.get(ENV_TAG_COMMONS) == null ? "" : argsMap.get(ENV_TAG_COMMONS).toString();
        bizPath = argsMap.get(BIZ_PATH_COMMONS) == null ? "" : argsMap.get(BIZ_PATH_COMMONS).toString();
        subBusiness = argsMap.get(SUB_BUSINESS_COMMONS) == null ? "" : argsMap.get(SUB_BUSINESS_COMMONS).toString();
        envSecret = argsMap.get(ENV_SECRET_COMMONS) == null ? "" : argsMap.get(ENV_SECRET_COMMONS).toString();
        appType = argsMap.get(APP_TYPE_COMMON) == null ? 0 : (Integer) argsMap.get(APP_TYPE_COMMON);
    }

    public static long getEnvId() {
        return envId;
    }

    public static String getCombinedEnvName() {
        return (StringUtils.isBlank(bizPath) ? DEFAULT_BUSINESS : bizPath) + ":" + appName + ":" + (StringUtils.isBlank(
                env) ? DEFAULT_ENV : env);
    }

    public static void setEnvId(int envId) {
        IdentityConfigManager.envId = envId;
    }

    public static long getAppId() {
        return appId;
    }

    public static void setAppId(int appId) {
        IdentityConfigManager.appId = appId;
    }

    public static long getBizId() {
        return bizId;
    }

    public static void setBizId(long bizId) {
        IdentityConfigManager.bizId = bizId;
    }

    public static int getDomainId() {
        return domainId;
    }

    public static void setDomainId(int domainId) {
        IdentityConfigManager.domainId = domainId;
    }

    public static String getAppName() {
        return appName;
    }

    public static int getAppType() {
        return appType;
    }

    public static void setAppName(String appName) {
        IdentityConfigManager.appName = appName;
    }

    public static String getInstanceName() {
        return instanceName;
    }

    public static void setInstanceName(String instanceName) {
        IdentityConfigManager.instanceName = instanceName;
    }

    public static String getEnv() {
        return env;
    }

    public static void setEnv(String env) {
        IdentityConfigManager.env = env;
    }

    public static String getEnvTag() {
        return envTag;
    }

    public static void setEnvTag(String envTag) {
        IdentityConfigManager.envTag = envTag;
    }

    public static String getBizPath() {
        return bizPath;
    }

    public static void setBizPath(String bizPath) {
        IdentityConfigManager.bizPath = bizPath;
    }

    public static long getInstanceId() {
        return instanceId;
    }

    public static void setInstanceId(long instanceId) {
        IdentityConfigManager.instanceId = instanceId;
    }

    public static String getSubBusiness() {
        return subBusiness;
    }

    public static void setSubBusiness(String subBusiness) {
        IdentityConfigManager.subBusiness = subBusiness;
    }

    public static Map<String, String> getAttachment() {
        return attachment;
    }

    public static void setAttachment(Map<String, String> attachment) {
        IdentityConfigManager.attachment = attachment;
    }

    public static String getEnvSecret() {
        return envSecret;
    }

    public static void setEnvId(long envId) {
        IdentityConfigManager.envId = envId;
    }

    public static void setAppId(long appId) {
        IdentityConfigManager.appId = appId;
    }

    public static void setEnvSecret(String envSecret) {
        IdentityConfigManager.envSecret = envSecret;
    }
}
