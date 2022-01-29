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

/*
 * Based on org/apache/skywalking/apm/agent/core/plugin/interceptor/enhance/OverrideCallable.java
 * from the Apache Skywalking project.
 */

package com.huawei.sermant.core.agent.common;

import com.huawei.sermant.core.agent.annotations.AboutDelete;

/**
 * 参数覆盖调用
 * <p> Copyright 2021
 *
 * @since 2021
 */
@AboutDelete
@Deprecated
public interface OverrideArgumentsCall {
    Object call(Object[] arguments);
}
