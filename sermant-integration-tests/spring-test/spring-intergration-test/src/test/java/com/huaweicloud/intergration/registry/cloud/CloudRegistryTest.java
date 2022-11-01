/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.intergration.registry.cloud;

import com.huaweicloud.intergration.common.utils.RequestUtils;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Collections;
import java.util.HashSet;

/**
 * SpringCloud注册测试
 *
 * @author zhouss
 * @since 2022-11-01
 */
public class CloudRegistryTest {
    @Rule(order = 300)
    public final TestRule rule = new CloudRegistryRule();

    /**
     * 核对测试结果
     */
    @Test
    public void check() {
        test(AppType.FEIGN);
        test(AppType.REST);
    }

    private void test(AppType appType) {
        final HashSet<String> ports = new HashSet<>();
        for (int i = 0; i < 2; i++) {
            ports.add(RequestUtils
                    .get(buildUrl(appType), Collections.emptyMap(), String.class));
        }
        Assert.assertTrue(ports.size() > 1);
    }

    private String buildUrl(AppType appType) {
        return getAddress(appType) + "/cloudRegistry/testCloudRegistry";
    }

    private String getAddress(AppType appType) {
        if (appType == AppType.FEIGN) {
            return "http://localhost:8015";
        } else {
            return "http://localhost:8005";
        }
    }

    enum AppType {
        FEIGN,
        REST
    }
}
