/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.sermant.core.plugin.inject;

import com.huaweicloud.sermant.core.service.BaseService;

import java.util.List;
import java.util.Map;

/**
 * spring注入类定义
 *
 * @author zhouss
 * @since 2022-06-22
 */
public interface ClassInjectService extends BaseService {
    /**
     * 针对低版本SpringFactories注入, 对应方法loadFactoryNames
     *
     * @param factoryName 注入工厂名
     * @param target 注入目标对象, 为该方法的的返回结果
     * @param classInjectDefine 注入目标
     * @param classLoader 指定类加载器, 该类加载器大部分情况为宿主classLoader
     * @return 注入的结果
     */
    List<String> injectConfiguration(String factoryName, List<String> target,
            ClassInjectDefine classInjectDefine,
            ClassLoader classLoader);

    /**
     * 注入
     *
     * @param target 注入目标
     * @param classInjectDefine 注入定义
     * @param classLoader 指定类加载器, 该类加载器大部分情况为宿主classLoader
     * @param isUnmodifiable 是否为不可修改集合
     */
    void injectConfiguration(Map<String, List<String>> target, ClassInjectDefine classInjectDefine,
            ClassLoader classLoader, boolean isUnmodifiable);
}
