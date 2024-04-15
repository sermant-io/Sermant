/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.spring.beans.deal.interceptors;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.TypeFilter;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

/**
 * @Component assembly bean interception-specific implementation class testing
 *
 * @author chengyouling
 * @since 2023-03-27
 */
public class SpringComponentBeanInterceptorTest extends BaseSpringBeansTest {
    private ExecuteContext executeContext = ExecuteContext.forMemberMethod(
            new ClassPathBeanDefinitionScanner(new AnnotationConfigApplicationContext()), null, new Object[0],
            null, null);

    @Test
    public void testBefore() {
        BEANS_DEAL_CONFIG.setEnabled(true);
        BEANS_DEAL_CONFIG.setExcludeBeans(
                "com.huawei.spring.beans.deal.interceptors.SpringComponentBeanInterceptorTest");
        SpringComponentBeanInterceptor interceptior = new SpringComponentBeanInterceptor();
        ExecuteContext afterExecute = interceptior.before(executeContext);
        ClassPathBeanDefinitionScanner scanner = (ClassPathBeanDefinitionScanner) afterExecute.getObject();
        List<TypeFilter> result = (List<TypeFilter>) ReflectUtils.getFieldValue(scanner, "excludeFilters")
                .get();
        Assert.assertEquals(1, result.size());
    }
}
