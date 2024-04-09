/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.monitor.common;

/**
 * ordinary constant class
 *
 * @author zhp
 * @since 2022-11-02
 */
public class CommonConstant {
    /**
     * dubbo consumer
     */
    public static final String DUBBO_CONSUMER = "consumer";

    /**
     * dubbo provider
     */
    public static final String DUBBO_PROVIDER = "provider";

    /**
     * distinguish between dubbo callers: provider or consumer
     */
    public static final String DUBBO_SIDE = "side";

    /**
     * dubbo gets the current service name from the url
     *
     * @see org.apache.dubbo.common.URL#getParameter(String)
     * @see com.alibaba.dubbo.common.URL#getParameter(String)
     */
    public static final String DUBBO_APPLICATION = "application";

    private CommonConstant() {
    }
}
