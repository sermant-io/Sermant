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

package com.huaweicloud.intergration.removal;

import com.huaweicloud.intergration.common.utils.RequestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 离群实例摘除插件测试类
 *
 * @author zhp
 * @since 2023-03-16
 */
@EnabledIfSystemProperty(named = "sermant.integration.test.type", matches = "REMOVAL")
public class RemovalTest {
    private static final String REQ_URL = "http://127.0.0.1:8017/removal/boot/testRemoval";

    private static final String REST_REQ_URL = "http://127.0.0.1:8022/removal/boot/testRemoval";

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(10);

    private static final int REMOVAL_TIME = 20000;

    private static final int TEST_REMOVED_TIME = 10000;

    @Test
    public void testRemoval() {
        int reqFailNum = getReqFailNum(REMOVAL_TIME, REQ_URL);
        Assertions.assertTrue(reqFailNum != 0);
        reqFailNum = getReqFailNum(TEST_REMOVED_TIME, REQ_URL);
        Assertions.assertEquals(0, reqFailNum);

        reqFailNum = getReqFailNum(REMOVAL_TIME, REST_REQ_URL);
        Assertions.assertTrue(reqFailNum != 0);
        reqFailNum = getReqFailNum(TEST_REMOVED_TIME, REST_REQ_URL);
        Assertions.assertEquals(0, reqFailNum);
    }

    /**
     * 测试失败数量
     *
     * @return 失败数量
     */
    private static int getReqFailNum(long time, String url) {
        AtomicInteger reqFailNum = new AtomicInteger();
        EXECUTOR_SERVICE.execute(() -> {
            long currentTimes = System.currentTimeMillis();
            while (System.currentTimeMillis() - currentTimes <= time) {
                try {
                    RequestUtils.get(url, new HashMap<>(), String.class);
                } catch (Exception e) {
                    reqFailNum.incrementAndGet();
                }
            }
        });
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return reqFailNum.get();
    }
}
