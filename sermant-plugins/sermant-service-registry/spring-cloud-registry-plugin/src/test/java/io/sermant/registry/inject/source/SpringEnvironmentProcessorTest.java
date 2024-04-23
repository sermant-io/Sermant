/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sermant.registry.inject.source;

import static org.junit.Assert.*;

import io.sermant.core.utils.ReflectUtils;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.Iterator;
import java.util.Optional;

/**
 * Environment variable testing
 *
 * @author zhouss
 * @since 2022-09-06
 */
public class SpringEnvironmentProcessorTest {
    @Test
    public void test() {
        final SpringEnvironmentProcessor springEnvironmentProcessor = new SpringEnvironmentProcessor();
        final ConfigurableEnvironment environment = Mockito.mock(ConfigurableEnvironment.class);
        final MutablePropertySources propertySources = new MutablePropertySources();
        Mockito.when(environment.getPropertySources()).thenReturn(propertySources);
        springEnvironmentProcessor.postProcessEnvironment(environment, null);
        Assert.assertTrue(propertySources.size() > 0);
        final Optional<Object> sourceName = ReflectUtils.getFieldValue(springEnvironmentProcessor, "SOURCE_NAME");
        Assert.assertTrue(sourceName.isPresent());
        propertySources.remove((String) sourceName.get());
        assertEquals(0, propertySources.size());
        propertySources.addFirst(new CompositePropertySource("test1"));
        propertySources.addFirst(new CompositePropertySource("test2"));
        propertySources.addFirst(new CompositePropertySource("test3"));
        final Optional<Object> dynamicPropertyName = ReflectUtils.getFieldValue(springEnvironmentProcessor, "DYNAMIC_PROPERTY_NAME");
        Assert.assertTrue(dynamicPropertyName.isPresent());
        propertySources.addFirst(new CompositePropertySource((String) dynamicPropertyName.get()));
        springEnvironmentProcessor.postProcessEnvironment(environment, null);
        final Iterator<PropertySource<?>> iterator = propertySources.iterator();
        int index = 2;
        PropertySource<?> expectedSource = null;
        while (iterator.hasNext() && index-- > 0) {
            expectedSource = iterator.next();
        }
        Assert.assertNotNull(expectedSource);
        Assert.assertEquals(expectedSource.getName(), sourceName.get());
    }
}
