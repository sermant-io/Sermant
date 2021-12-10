/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.javamesh.core.lubanops.integration.authorization;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.huawei.javamesh.core.lubanops.integration.transport.http.HttpSigner;

/**
 * HttpSigner Tester.
 * @author <Authors name>
 * @version 1.0
 * @since
 * 
 *        <pre>
 * 8�� 10, 2020
 *        </pre>
 */
public class HttpSignerTest {

    HttpSigner signer;

    @Before
    public void before() throws Exception {

        signer = new HttpSigner();
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: sign(Request request)
     */
    @Test
    public void testSign() throws Exception {
    }

    /**
     * Method: verify(Request request)
     */
    @Test
    public void testVerify() throws Exception {
        // TODO: Test goes here...
    }

}
