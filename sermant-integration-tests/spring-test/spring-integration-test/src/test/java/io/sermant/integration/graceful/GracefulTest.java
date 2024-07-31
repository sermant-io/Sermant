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

package io.sermant.integration.graceful;

import io.sermant.integration.common.utils.EnvUtils;
import io.sermant.integration.common.utils.RequestUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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
@Disabled
public abstract class GracefulTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(GracefulTest.class);

    private static final int MIN_PORT_SIZE = 2;

    /**
     * 最小倍数
     */
    private static final int MIN_RATE = 2;

    private static final int UP_REQUEST_COUNT = 500;

    private static final int DOWN_REQUEST_COUNT = 1000;

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
        for (int i = 0; i < 4; i++) {
            try {
                for (int j = 0; j < UP_REQUEST_COUNT; j++) {
                    statistic(statisticMap);
                }
                Thread.sleep(10000);
            } catch (InterruptedException exception) {
                LOGGER.error(exception.getMessage(), exception);
            }
        }
        final Collection<Integer> values = statisticMap.values();
        LOGGER.info("values: {}", statisticMap.values());
        if (values.size() < MIN_PORT_SIZE) {
            Assertions.fail();
        }
        final Iterator<Integer> iterator = values.iterator();
        int portCount1 = iterator.next();
        int portCount2 = iterator.next();
        LOGGER.info("request result: {}", statisticMap);
        if (portCount2 > portCount1) {
            Assertions.assertTrue((portCount2 / portCount1) >= MIN_RATE);
        } else {
            Assertions.assertTrue((portCount1 / portCount2) >= MIN_RATE);
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
            for (int i = 0; i < DOWN_REQUEST_COUNT; i++) {
                RequestUtils.get(buildUrl("testGraceful"), Collections.emptyMap(), String.class);
            }
        } catch (Exception exception) {
            LOGGER.error(exception.getMessage(), exception);
            Assertions.fail();
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

    protected abstract String getBaseUrl();
}
