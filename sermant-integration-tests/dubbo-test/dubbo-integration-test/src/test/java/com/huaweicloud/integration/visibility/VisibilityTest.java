/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.integration.visibility;

import com.huaweicloud.integration.utils.RequestUtils;
import com.huaweicloud.integration.visibility.entity.ServerInfo;

import com.alibaba.fastjson.JSONArray;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;

/**
 * 服务可见性测试
 *
 * @author zhp
 * @since 2023-01-04
 */
@EnabledIfEnvironmentVariable(named = "TEST_TYPE", matches = "visibility")
public class VisibilityTest {
    /**
     * 服务信息采集URL
     */
    private static final String URL = "http://127.0.0.1:8900/visibility/getCollectorInfo";

    @Test
    public void testVisibility() throws InterruptedException {
        Thread.sleep(20000);
        String string = RequestUtils.get(URL, new HashMap<>(), String.class);
        Assert.notNull(string, "服务信息采集失败");
        List<ServerInfo> serverInfos = JSONArray.parseArray(string, ServerInfo.class);
        Assert.notNull(serverInfos, "服务信息采集失败");
        serverInfos.forEach(serverInfo -> {
            Assert.notNull(serverInfo.getApplicationName(), "应用名称为空");
            Assert.notNull(serverInfo.getGroupName(), "服务组名称为空");
            Assert.notNull(serverInfo.getVersion(), "版本号信息为空");
            if (serverInfo.getConsanguinityList() != null && !serverInfo.getConsanguinityList().isEmpty()) {
                serverInfo.getConsanguinityList().forEach(consanguinity ->
                        Assert.notEmpty(consanguinity.getProviders(), "服务提供者信息为空"));
            }
            Assert.notEmpty(serverInfo.getContractList(), "契约信息为空");
            serverInfo.getContractList().forEach(contract ->
                    Assert.notEmpty(contract.getMethodInfoList(),
                            "契约信息中" + contract.getInterfaceName() + "方法信息为空"));
        });
    }
}
