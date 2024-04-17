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

package com.huawei.dubbo.registry;

/**
 * Test constants
 *
 * @author provenceee
 * @since 2022-02-16
 */
public class TestConstant {
    /**
     * Anonymous fields
     */
    public static final String FOO = "foo";

    /**
     * Anonymous fields
     */
    public static final String BAR = "bar";

    /**
     * The registered address of the SC protocol
     */
    public static final String SC_ADDRESS = "sc://localhost:30100";

    /**
     * The registered address of the NACOS protocol
     */
    public static final String NACOS_ADDRESS = "nacos://127.0.0.1:8848";

    private TestConstant() {
    }
}
