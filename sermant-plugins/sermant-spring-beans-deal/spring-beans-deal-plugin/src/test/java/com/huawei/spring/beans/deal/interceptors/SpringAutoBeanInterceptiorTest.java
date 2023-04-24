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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

/**
 * 自动装配bean拦截具体实现内容测试
 *
 * @author chengyouling
 * @since 2023-03-27
 */
public class SpringAutoBeanInterceptiorTest extends BaseSpringBeansTest{
    private ExecuteContext executeContext = ExecuteContext.forMemberMethod(new Object(), null, new Object[0],
            null, null);
    private static final String SPRING_BOOT_AUTOCONFIGURE =
            "org.springframework.boot.autoconfigure.EnableAutoConfiguration";

    @Test
    public void testAfter() {
        BEANS_DEAL_CONFIG.setEnabled(true);
        BEANS_DEAL_CONFIG.setExcludeAutoConfigurations("A");
        Map<String, List<String>> result = new HashMap<>();
        List<String> beanNames = new ArrayList<>();
        beanNames.add("A");
        beanNames.add("B");
        beanNames.add("C");
        result.put(SPRING_BOOT_AUTOCONFIGURE, beanNames);
        executeContext.afterMethod(result, new Throwable());
        SpringAutoBeanInterceptor interceptior = new SpringAutoBeanInterceptor();
        ExecuteContext afterExecuteContext = interceptior.after(executeContext);
        Assert.assertEquals(2,
            ((Map<String, List<String>>) afterExecuteContext.getResult()).get(SPRING_BOOT_AUTOCONFIGURE).size());
    }
}
