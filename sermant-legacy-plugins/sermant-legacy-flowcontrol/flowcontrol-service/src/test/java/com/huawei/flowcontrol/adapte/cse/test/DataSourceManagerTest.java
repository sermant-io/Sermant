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

package com.huawei.flowcontrol.adapte.cse.test;

import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.flowcontrol.core.datasource.DefaultDataSourceManager;
import com.huawei.flowcontrol.core.datasource.zookeeper.ZookeeperDatasourceManager;
import org.junit.Before;
import org.junit.Test;

/**
 * 测试数据源初始化
 *
 * @author zhouss
 * @since 2021-12-25
 */
public class DataSourceManagerTest extends BaseTest {

    @Before
    public void config() {
        configManagerMap.put("flow.control.plugin", new FlowControlConfig());
    }

    @Test
    public void testStart() {
        new ZookeeperDatasourceManager().start();
        new DefaultDataSourceManager().start();
    }
}
