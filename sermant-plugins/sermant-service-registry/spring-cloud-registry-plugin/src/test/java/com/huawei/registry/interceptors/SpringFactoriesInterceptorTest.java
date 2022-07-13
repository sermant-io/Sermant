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

package com.huawei.registry.interceptors;

import com.huaweicloud.sermant.core.utils.ReflectUtils;

import com.huaweicloud.sermant.core.plugin.inject.ClassInjectDefine;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 测试注入逻辑
 *
 * @author zhouss
 * @since 2022-06-29
 */
public class SpringFactoriesInterceptorTest {
    private static final String[] DEFINES = {
            "com.huawei.registry.inject.ContextClosedEventListenerInjectDefine",
            "com.huawei.registry.inject.RequestInterceptorInjectDefine",
            "com.huawei.registry.inject.OriginRegistrySwitchInjectDefine",
            "com.huawei.registry.inject.ScConfigurationInjectDefine",
            "com.huawei.registry.inject.RibbonConfigurationDefine",
            "com.huawei.registry.inject.ScServerInjectDefine",
            "com.huawei.registry.inject.RegistrationPropertiesInjectDefine"
    };

    /**
     * 测试spi加载
     */
    @Test
    public void testClassDefine() {
        final SpringFactoriesInterceptor springFactoriesInterceptor = new SpringFactoriesInterceptor();
        final Optional<Object> classDefines = ReflectUtils.getFieldValue(springFactoriesInterceptor, "CLASS_DEFINES");
        Assert.assertTrue(classDefines.isPresent() && classDefines.get() instanceof List);
        final List<ClassInjectDefine> classInjectDefines = (List<ClassInjectDefine>) classDefines.get();
        Assert.assertTrue(classInjectDefines.size() >= DEFINES.length);
        Assert.assertTrue(checkDefines(classInjectDefines));
    }

    private boolean checkDefines(List<ClassInjectDefine> classInjectDefines) {
        final List<String> collect = classInjectDefines.stream()
                .map(classInjectDefine -> classInjectDefine.getClass().getName()).collect(Collectors.toList());
        for (String className : DEFINES) {
            if (!collect.contains(className)) {
                return false;
            }
        }
        return true;
    }
}
