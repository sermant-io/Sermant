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

package io.sermant.core.service.inject;

import io.sermant.core.service.BaseService;

import java.util.List;
import java.util.Map;

/**
 * spring injection class definition
 *
 * @author zhouss
 * @since 2022-06-22
 */
public interface ClassInjectService extends BaseService {
    /**
     * For injection of earlier SpringFactories, corresponding method loadFactoryNames
     *
     * @param factoryName Injection factory name
     * @param target target object as the return result of the method
     * @param classInjectDefine classInjectDefine
     * @param classLoader Specifies the classloader, which in most cases is the host classLoader
     * @return inject result
     */
    List<String> injectConfiguration(String factoryName, List<String> target,
            ClassInjectDefine classInjectDefine,
            ClassLoader classLoader);

    /**
     * injection
     *
     * @param target target
     * @param classInjectDefine classInjectDefine
     * @param classLoader Specifies the classloader, which in most cases is the host classLoader
     * @param isUnmodifiable isUnmodifiable
     */
    void injectConfiguration(Map<String, List<String>> target, ClassInjectDefine classInjectDefine,
            ClassLoader classLoader, boolean isUnmodifiable);
}
