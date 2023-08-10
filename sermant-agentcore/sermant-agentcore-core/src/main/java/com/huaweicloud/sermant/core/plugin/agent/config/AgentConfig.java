/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.core.plugin.agent.config;

import com.huaweicloud.sermant.core.config.common.BaseConfig;
import com.huaweicloud.sermant.core.config.common.ConfigTypeKey;

import java.util.Collections;
import java.util.Set;

/**
 * 增强配置类
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-25
 */
@ConfigTypeKey("agent.config")
public class AgentConfig implements BaseConfig {
    /**
     * 是否允许字节码增强重转换
     */
    private boolean isReTransformEnable = false;

    /**
     * 增强忽略集，该集合中定义的全限定名前缀用于排除增强过程中被忽略的类，默认包含{@code com.huawei.sermant}，非强制
     */
    private Set<String> ignoredPrefixes = Collections.singleton("com.huawei.sermant");

    /**
     * 增强忽略接口集，该集合中定义的接口用于排除增强过程中被忽略的类，默认包含{@org.springframework.cglib.proxy.Factory}，强制，否则会跟spring动态代理产生冲突，导致java.lang.VerifyError
     */
    private Set<String> ignoredInterfaces = Collections.singleton("org.springframework.cglib.proxy.Factory");

    /**
     * 是否在增强过程中输出检索日志
     */
    private boolean isShowEnhanceLog = false;

    /**
     * 是否输出被增强的类的字节码文件
     */
    private boolean isOutputEnhancedClasses = false;

    /**
     * 被增强类的输出路径，如果为空，则不输出
     */
    private String enhancedClassesOutputPath;

    /**
     * 拦截插件服务名单
     */
    private Set<String> serviceInjectList = Collections.emptySet();

    /**
     * 允许从线程上下文中加载类，主要用于插件类加载器通过线程上下文类加载宿主服务的类，如果不允许可以在拦截器调用过程中指定
     */
    private boolean useContextLoader = false;

    public boolean isReTransformEnable() {
        return isReTransformEnable;
    }

    public void setReTransformEnable(boolean reTransformEnable) {
        isReTransformEnable = reTransformEnable;
    }

    public Set<String> getIgnoredPrefixes() {
        return ignoredPrefixes;
    }

    public Set<String> getIgnoredInterfaces() {
        return ignoredInterfaces;
    }

    public void setIgnoredInterfaces(Set<String> ignoredInterfaces) {
        this.ignoredInterfaces = ignoredInterfaces;
    }

    public void setIgnoredPrefixes(Set<String> ignoredPrefixes) {
        this.ignoredPrefixes = ignoredPrefixes;
    }

    public boolean isShowEnhanceLog() {
        return isShowEnhanceLog;
    }

    public void setShowEnhanceLog(boolean showEnhanceLog) {
        isShowEnhanceLog = showEnhanceLog;
    }

    public boolean isOutputEnhancedClasses() {
        return isOutputEnhancedClasses;
    }

    public void setOutputEnhancedClasses(boolean outputEnhancedClasses) {
        isOutputEnhancedClasses = outputEnhancedClasses;
    }

    public String getEnhancedClassesOutputPath() {
        return enhancedClassesOutputPath;
    }

    public void setEnhancedClassesOutputPath(String enhancedClassesOutputPath) {
        this.enhancedClassesOutputPath = enhancedClassesOutputPath;
    }

    public Set<String> getServiceInjectList() {
        return serviceInjectList;
    }

    public void setServiceInjectList(Set<String> serviceInjectList) {
        this.serviceInjectList = serviceInjectList;
    }

    public boolean isUseContextLoader() {
        return useContextLoader;
    }

    public void setUseContextLoader(boolean useContextLoader) {
        this.useContextLoader = useContextLoader;
    }
}
