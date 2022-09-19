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

package com.huawei.flowcontrol.common.core.resolver;

import com.huawei.flowcontrol.common.core.rule.AbstractRule;

import com.huaweicloud.sermant.core.operation.OperationManager;
import com.huaweicloud.sermant.core.operation.converter.api.YamlConverter;
import com.huaweicloud.sermant.implement.operation.converter.YamlConverterImpl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Optional;

/**
 * 规则解析测试
 *
 * @author zhouss
 * @since 2022-08-29
 */
public abstract class AbstractRuleResolverTest<T extends AbstractRule> {
    private MockedStatic<OperationManager> operationManagerMockedStatic;

    @Before
    public void setUp() {
        operationManagerMockedStatic = Mockito.mockStatic(OperationManager.class);
        operationManagerMockedStatic.when(() -> OperationManager.getOperation(YamlConverter.class)).thenReturn(new YamlConverterImpl());
    }

    @After
    public void tearDown() throws Exception {
        operationManagerMockedStatic.close();
    }

    @Test
    public void test() {
        final AbstractResolver<T> resolver = getResolver();
        Assert.assertEquals(resolver.getConfigKey(), getConfigKey());
        final Optional<T> test = resolver.parseRule(getKey(), getValue(), true, false);
        Assert.assertTrue(test.isPresent());
        checkAttrs(test.get());
        final Optional<T> delete = resolver.parseRule(getKey(), getValue(), true, true);
        Assert.assertFalse(delete.isPresent());
    }

    /**
     * 下发配置键
     *
     * @return 配置键
     */
    public String getKey() {
        return getConfigKey() + "." + getBusinessKey();
    }

    /**
     * 获取业务场景名
     *
     * @return 业务场景名
     */
    public String getBusinessKey() {
        return "test";
    }

    /**
     * 获取解析器
     *
     * @return 解析器
     */
    public abstract AbstractResolver<T> getResolver();

    /**
     * 获取配置键
     *
     * @return 配置键
     */
    public abstract String getConfigKey();

    /**
     * 获取配置值
     *
     * @return 配置
     */
    public abstract String getValue();

    /**
     * 判断属性是否正确
     *
     * @param rule 解析的对象
     */
    public abstract void checkAttrs(T rule);
}
