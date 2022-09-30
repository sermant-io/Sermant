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

package com.huaweicloud.integration.router;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 区域路由测试
 *
 * @author provenceee
 * @since 2022-09-28
 */
@EnabledIfEnvironmentVariable(named = "TEST_TYPE", matches = "zone")
public class ZoneRouterTest {
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static final String BASE_URL = "http://127.0.0.1:28020/consumer/";

    private static final int TIMES = 30;

    private static final int WAIT_SECONDS = 30;

    private final String zone;

    /**
     * 构造方法
     */
    public ZoneRouterTest() {
        zone = Optional.ofNullable(System.getenv("SERVICE_META_ZONE")).orElse("bar");
    }

    /**
     * 测试区域路由
     */
    @Test
    public void testZoneRouter() throws InterruptedException {
        // 调用相同区域实例30次
        for (int i = 0; i < TIMES; i++) {
            Assertions.assertEquals(zone, REST_TEMPLATE.getForObject(BASE_URL + "getZone?exit=false", String.class));
        }

        // 停掉相同区域实例
        Assertions.assertThrows(Exception.class, () -> Assertions
            .assertEquals(zone, REST_TEMPLATE.getForObject(BASE_URL + "getZone?exit=true", String.class)));

        // 等待实例下线
        for (int i = 0; i < WAIT_SECONDS; i++) {
            try {
                if (!zone.equals(REST_TEMPLATE.getForObject(BASE_URL + "getZone?exit=false", String.class))) {
                    // 下游实例已下线
                    break;
                }
            } catch (Exception ignored) {
                // 下游实例还未剔除，忽略
            }
            TimeUnit.SECONDS.sleep(1);
        }

        // 调用不同区域实例30次
        for (int i = 0; i < TIMES; i++) {
            Assertions.assertNotEquals(zone, REST_TEMPLATE.getForObject(BASE_URL + "getZone?exit=false", String.class));
        }
    }
}