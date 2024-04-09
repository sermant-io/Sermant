/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.flowcontrol.common.entity;

import static org.junit.Assert.assertEquals;

import com.huawei.flowcontrol.common.entity.HttpRequestEntity.Builder;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * test hashcode
 *
 * @author zhouss
 * @since 2022-08-29
 */
public class RequestEntityTest {
    /**
     * test hash
     */
    @Test
    public void test() {
        String serviceName = "test";
        String apiPath = "/api";
        final Builder builder1 = new Builder();
        builder1.setServiceName(serviceName);
        builder1.setHeaders(buildHeaders());
        builder1.setApiPath(apiPath);

        final Builder builder2 = new Builder();
        builder2.setServiceName(serviceName);
        builder2.setHeaders(buildHeaders());
        builder2.setApiPath(apiPath);

        assertEquals(builder2.build(), builder1.build());
    }

    private Map<String, String> buildHeaders() {
        final Map<String, String> headers = new HashMap<>();
        headers.put("token", "aasdjosajdosadpsakdpsakpdjsjdnsa");
        headers.put("api", "/test/aaa");
        return headers;
    }
}
