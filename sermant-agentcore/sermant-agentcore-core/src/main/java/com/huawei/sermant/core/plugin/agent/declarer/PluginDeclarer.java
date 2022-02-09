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

package com.huawei.sermant.core.plugin.agent.declarer;

import com.huawei.sermant.core.plugin.agent.collector.PluginCollectorManager;
import com.huawei.sermant.core.plugin.agent.matcher.ClassMatcher;

/**
 * 插件声明，{@link PluginDescription}的高阶api
 * <p>该接口在组装时会尝试合并，见于{@link PluginCollectorManager}
 * <p>因此建议使用者优先使用该接口定义增强插件
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-24
 */
public interface PluginDeclarer {
    /**
     * 获取插件的类匹配器
     *
     * @return 类匹配器
     */
    ClassMatcher getClassMatcher();

    /**
     * 获取插件的拦截声明
     *
     * @param classLoader 被增强类的类加载器
     * @return 拦截声明集
     */
    InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader);

    /**
     * 获取插件的超类声明
     *
     * @return 超类声明集
     */
    SuperTypeDeclarer[] getSuperTypeDeclarers();
}
