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

package com.huawei.discovery.entity;

import static org.junit.Assert.*;

import org.apache.http.ProtocolVersion;
import org.junit.Assert;
import org.junit.Test;

/**
 * 错误响应测试
 *
 * @author zhouss
 * @since 2022-10-12
 */
public class ErrorCloseableHttpResponseTest {
    @Test
    public void test() {
        final Exception exception = new Exception("wrong");
        final ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
        final ErrorCloseableHttpResponse response = new ErrorCloseableHttpResponse(exception,
                protocolVersion);
        Assert.assertEquals(response.getProtocolVersion(), protocolVersion);
        assertNotNull(response.getEntity());
    }
}
