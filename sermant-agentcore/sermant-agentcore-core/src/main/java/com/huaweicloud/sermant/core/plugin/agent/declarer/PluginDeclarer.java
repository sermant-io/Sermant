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

package com.huaweicloud.sermant.core.plugin.agent.declarer;

import com.huaweicloud.sermant.core.plugin.agent.collector.PluginCollector;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;

/**
 * PluginDeclarer，high level api for {@link PluginDescription}
 * <p>The interface attempts to merge when assembled, as shown in {@link PluginCollector}
 * <p>Therefore, it is recommended that users use this interface first to define enhanced plugins
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-24
 */
public interface PluginDeclarer {
    /**
     * Gets the class matcher for the plugin
     *
     * @return class matcher
     */
    ClassMatcher getClassMatcher();

    /**
     * Gets the plugin's InterceptDeclarers
     *
     * @param classLoader The classLoader of the enhanced class
     * @return InterceptDeclarer set
     */
    InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader);

    /**
     * Gets the superclass declarers for the plugin
     *
     * @return SuperTypeDeclarer set
     */
    SuperTypeDeclarer[] getSuperTypeDeclarers();

    /**
     * It is up to the plugin declarator to decide if the declared method needs to be enhanced. The default is TRUE
     *
     * @return result
     */
    default boolean isEnabled() {
        return true;
    }
}
