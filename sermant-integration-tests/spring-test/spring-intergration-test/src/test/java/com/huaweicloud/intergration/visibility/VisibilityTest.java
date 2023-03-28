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

package com.huaweicloud.intergration.visibility;

import com.huaweicloud.intergration.common.utils.RequestUtils;
import com.huaweicloud.intergration.visibility.entity.ServerInfo;

import com.alibaba.fastjson.JSONArray;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.HashMap;
import java.util.List;

/**
 * 监控测试类
 *
 * @author ZHP
 * @since 2002-11-24
 */
@EnabledIfSystemProperty(named = "sermant.integration.test.type", matches = "VISIBILITY")
public class VisibilityTest {
    /**
     * 服务信息采集URL
     */
    private static final String URL = "http://127.0.0.1:8900/visibility/getCollectorInfo";

    private static final String TEST_URL = "http://127.0.0.1:8015/cloudRegistry/testCloudRegistry";

    @Test
    public void testVisibility() throws InterruptedException {
        RequestUtils.get(TEST_URL, new HashMap<>(), String.class);
        Thread.sleep(30000);
        String string = RequestUtils.get(URL, new HashMap<>(), String.class);
        Assertions.assertNotNull(string, "服务信息采集失败");
        List<ServerInfo> serverInfos = JSONArray.parseArray(string, ServerInfo.class);
        Assertions.assertNotNull(serverInfos, "服务信息采集失败");
        serverInfos.forEach(serverInfo -> {
            Assertions.assertNotNull(serverInfo.getApplicationName(), "应用名称为空");
            Assertions.assertNotNull(serverInfo.getGroupName(), "服务组名称为空");
            Assertions.assertNotNull(serverInfo.getVersion(), "版本号信息为空");
            if (serverInfo.getConsanguinityList() != null && !serverInfo.getConsanguinityList().isEmpty()) {
                serverInfo.getConsanguinityList().forEach(consanguinity ->
                        Assertions.assertFalse(consanguinity.getProviders().isEmpty(), "服务提供者信息为空"));
            }
            if (serverInfo.getContractList() != null && !serverInfo.getContractList().isEmpty()) {
                serverInfo.getContractList().forEach(contract ->
                        Assertions.assertFalse(contract.getMethodInfoList().isEmpty(),
                                "契约信息中" + contract.getInterfaceName() + "方法信息为空"));
            }
        });
    }
}
