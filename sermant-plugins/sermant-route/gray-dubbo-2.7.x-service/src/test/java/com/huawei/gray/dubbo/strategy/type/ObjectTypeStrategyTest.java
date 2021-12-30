/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.gray.dubbo.strategy.type;

import com.huawei.gray.dubbo.strategy.TypeStrategy;
import com.huawei.sermant.core.common.CommonConstant;
import com.huawei.sermant.core.common.LoggerFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 实体匹配策略测试
 *
 * @author pengyuyi
 * @date 2021/12/1
 */
public class ObjectTypeStrategyTest {
    /**
     * 初始化
     */
    @Before
    public void before() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(CommonConstant.LOG_SETTING_FILE_KEY, getClass().getResource("/logback-test.xml").getPath());
        LoggerFactory.init(map);
    }

    /**
     * 测试实体策略
     */
    @Test
    public void testValue() {
        TypeStrategy strategy = new ObjectTypeStrategy();
        Entity entity = new Entity();
        entity.setTest("bar");
        // 正常情况
        Assert.assertEquals("bar", strategy.getValue(entity, ".test"));
        // 测试null
        Assert.assertNotEquals("bar", strategy.getValue(new Entity(), ".test"));
        // 测试找不到字段
        Assert.assertNull(strategy.getValue(new Entity(), ".foo"));
        // 测试不等于
        entity.setTest("foo");
        Assert.assertNotEquals("bar", strategy.getValue(entity, ".test"));
    }

    /**
     * 实体
     */
    public static class Entity {
        private String test;

        public String getTest() {
            return test;
        }

        public void setTest(String test) {
            this.test = test;
        }
    }
}
