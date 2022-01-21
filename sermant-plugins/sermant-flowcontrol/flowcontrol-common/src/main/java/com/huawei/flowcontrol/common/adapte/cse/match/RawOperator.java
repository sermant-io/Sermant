/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Based on org/apache/servicecomb/governance/marker/operator/RawOperator.java
 * from the Apache ServiceComb Java Chassis project.
 */

package com.huawei.flowcontrol.common.adapte.cse.match;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于kv格式数据存储
 *
 * @author zhouss
 * @since 2021-11-16
 */
public class RawOperator extends HashMap<String, String> {
    /**
     * 默认初始化大小
     */
    private static final int DEFAULT_CAPACITY = 4;

    public RawOperator() {
        super(DEFAULT_CAPACITY);
    }

    public RawOperator(Map<String, String> source) {
        super(source);
    }
}
