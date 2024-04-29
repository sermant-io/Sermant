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

package io.sermant.flowcontrol.res4j.chain.handler;

import io.sermant.flowcontrol.res4j.chain.HandlerChainEntry;

/**
 * test interface
 *
 * @author zhouss
 * @since 2022-08-30
 */
public interface RequestTest {
    /**
     * service scenario name
     */
    String BUSINESS_NAME = "test";

    /**
     * test path
     */
    String API_PATH = "/test";

    /**
     * execute the test
     */
    void test(HandlerChainEntry entry, String sourceName);

    /**
     * publishing rule
     */
    void publishRule();

    /**
     * clear
     */
    void clear();

    /**
     * build key
     *
     * @param prefix prefix
     * @return key
     */
    default String buildKey(String prefix) {
        return prefix + "." + BUSINESS_NAME;
    }
}
