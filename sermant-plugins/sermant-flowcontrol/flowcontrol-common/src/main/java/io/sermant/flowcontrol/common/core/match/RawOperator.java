/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

/**
 * Based on org/apache/servicecomb/governance/marker/operator/RawOperator.java from the Apache ServiceComb Java Chassis
 * project.
 */

package io.sermant.flowcontrol.common.core.match;

import java.util.HashMap;
import java.util.Map;

/**
 * for kv format data storage
 *
 * @author zhouss
 * @since 2021-11-16
 */
public class RawOperator extends HashMap<String, String> {
    /**
     * default initialization size
     */
    private static final int DEFAULT_CAPACITY = 4;
    private static final long serialVersionUID = 1930797351862384146L;

    /**
     * constructor
     */
    public RawOperator() {
        super(DEFAULT_CAPACITY);
    }

    /**
     * constructor
     *
     * @param source data
     */
    public RawOperator(Map<String, String> source) {
        super(source);
    }
}
