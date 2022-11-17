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

package com.huaweicloud.intergration.graceful;

import com.huaweicloud.intergration.common.rule.DisableRule;
import com.huaweicloud.intergration.common.utils.EnvUtils;
import com.huaweicloud.intergration.common.utils.RequestUtils;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * 优雅上下线测试
 *
 * @author zhouss
 * @since 2022-11-14
 */
public class GracefulTest {
    @Rule(order = 0)
    public final TestRule rule = new DisableRule();

    private static final Logger LOGGER = LoggerFactory.getLogger(GracefulTest.class);

    private static final int MIN_PORT_SIZE = 2;

    /**
     * 最小倍数
     */
    private static final int MIN_RATE = 2;

    private static final int REQUEST_COUNT = 1000;

    private final String url = getBaseUrl();

    /**
     * 测试优雅上下线
     */
    @Test
    public void testGracefulUp() {
        if (!isTargetTest("up")) {
            return;
        }
        final Map<String, Integer> statisticMap = new HashMap<>();
        for (int i = 0; i < REQUEST_COUNT; i++) {
            statistic(statisticMap);
        }
        final Collection<Integer> values = statisticMap.values();
        if (values.size() < MIN_PORT_SIZE) {
            Assert.fail();
        }
        final Iterator<Integer> iterator = values.iterator();
        int portCount1 = iterator.next();
        int portCount2 = iterator.next();
        LOGGER.info("request result: {}", statisticMap);
        if (portCount2 > portCount1) {
            Assert.assertTrue((portCount2 / portCount1) >= MIN_RATE);
        } else {
            Assert.assertTrue((portCount1 / portCount2) >= MIN_RATE);
        }
    }

    private boolean isTargetTest(String type) {
        final String env = EnvUtils.getEnv("graceful.test.type", null);
        return type.equalsIgnoreCase(env);
    }

    /**
     * 测试优雅下线
     */
    @Test
    public void testGracefulDown() {
        if (!isTargetTest("down")) {
            return;
        }
        try {
            for (int i = 0; i < REQUEST_COUNT; i++) {
                RequestUtils.get(buildUrl("testGraceful"), Collections.emptyMap(),
                        String.class);
            }
        } catch (Exception exception) {
            LOGGER.error(exception.getMessage(), exception);
            Assert.fail();
        }
    }

    private void statistic(Map<String, Integer> statisticMap) {
        final String port = RequestUtils.get(buildUrl("testGraceful"), Collections.emptyMap(),
                String.class);
        Integer count = statisticMap.getOrDefault(port, 0);
        statisticMap.put(port, ++count);
    }

    private String buildUrl(String api) {
        return String.format(Locale.ENGLISH, url + "/graceful/%s", api);
    }

    protected String getBaseUrl() {
        return "http://localhost:8005";
    }
}
