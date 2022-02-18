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
 * Based on org/apache/skywalking/apm/plugin/feign/http/v9/define/DefaultHttpClientInstrumentation.java
 * from the Apache Skywalking project.
 */

package com.huawei.gray.feign.definition;

/**
 * 拦截feign的ApacheHttpClient和OkHttpClient请求
 *
 * @author lilai
 * @since 2021-11-03
 */
public class DefaultHttpClientDefinition extends AbstractInstDefinition {
    /**
     * Intercept class.
     */
    private static final String INTERCEPT_CLASS = "com.huawei.gray.feign.interceptor.DefaultHttpClientInterceptor";

    /**
     * 构造方法
     */
    public DefaultHttpClientDefinition() {
        super(new String[]{"feign.Client$Default"}, INTERCEPT_CLASS, "execute");
    }
}