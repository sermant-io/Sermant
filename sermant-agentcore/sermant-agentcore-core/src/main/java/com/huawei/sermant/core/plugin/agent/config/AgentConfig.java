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

package com.huawei.sermant.core.plugin.agent.config;

import com.huawei.sermant.core.config.common.BaseConfig;
import com.huawei.sermant.core.config.common.ConfigTypeKey;

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
     * 是否增强启动类加载器加载的类
     */
    private boolean isEnhanceBootStrapEnable = false;

    /**
     * 增强忽略集，该集合中定义的全限定名前缀用于排除增强过程中被忽略的类，默认包含{@code com.huawei.sermant}，非强制
     */
    private Set<String> ignoredPrefixes = Collections.singleton("com.huawei.sermant");

    /**
     * 是否在增强过程中输出检索日志
     */
    private boolean isShowEnhanceLogEnable = false;

    /**
     * 被增强类的输出路径，如果为空，则不输出
     */
    private String enhancedClassOutputPath;

    /**
     * 插件的合并策略，定义{@link com.huawei.sermant.core.plugin.agent.declarer.PluginDeclarer}插件声明器的合并策略
     */
    private CombineStrategy combineStrategy = CombineStrategy.ALL;

    public boolean isEnhanceBootStrapEnable() {
        return isEnhanceBootStrapEnable;
    }

    @SuppressWarnings("checkstyle:RegexpSingleline")
    public void setEnhanceBootStrapEnable(boolean enhanceBootStrapEnable) {
        isEnhanceBootStrapEnable = enhanceBootStrapEnable;
    }

    public Set<String> getIgnoredPrefixes() {
        return ignoredPrefixes;
    }

    public void setIgnoredPrefixes(Set<String> ignoredPrefixes) {
        this.ignoredPrefixes = ignoredPrefixes;
    }

    public boolean isShowEnhanceLogEnable() {
        return isShowEnhanceLogEnable;
    }

    @SuppressWarnings("checkstyle:RegexpSingleline")
    public void setShowEnhanceLogEnable(boolean showEnhanceLogEnable) {
        isShowEnhanceLogEnable = showEnhanceLogEnable;
    }

    public String getEnhancedClassOutputPath() {
        return enhancedClassOutputPath;
    }

    public void setEnhancedClassOutputPath(String enhancedClassOutputPath) {
        this.enhancedClassOutputPath = enhancedClassOutputPath;
    }

    public CombineStrategy getCombineStrategy() {
        return combineStrategy;
    }

    public void setCombineStrategy(
            CombineStrategy combineStrategy) {
        this.combineStrategy = combineStrategy;
    }

    /**
     * 插件声明器的合并策略
     * <p>通常，以下策略差异不大，没有决定性的影响，一般取{@link #ALL}即可
     */
    public enum CombineStrategy {
        /**
         * 不合并，每个{@link com.huawei.sermant.core.plugin.agent.declarer.PluginDeclarer}都会被当做是一个transformer
         * <p>插件较少，且模糊匹配的较多的场景，该策略有优势
         */
        NONE,

        /**
         * 仅合并{@link com.huawei.sermant.core.plugin.agent.declarer.PluginDeclarer#getClassMatcher}为{@link
         * com.huawei.sermant.core.plugin.agent.matcher.ClassTypeMatcher}的插件声明器，即通过匹配的类名合并
         * <p>插件较多，且主要是全限定名匹配的场景时，该策略有优势
         */
        BY_NAME,

        /**
         * 对于所有{@link com.huawei.sermant.core.plugin.agent.declarer.PluginDeclarer}，都会进行合并
         */
        ALL
    }
}
