/*
 * Copyright (C) 2022-2025 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.xds.service.traffic.management.constant;

/**
 * handler constant
 *
 * @author zhouss
 * @since 2022-07-05
 */
public class HandlerConstants {
    /**
     * xds service processing priority
     */
    public static final int XDS_BUSINESS_ORDER = -2000;

    /**
     *  xds fault injection priority
     */
    public static final int XDS_FAULT_ORDER = 3000;

    /**
     * rate limiting priority
     */
    public static final int XDS_RATE_LIMIT_ORDER = 5000;

    private HandlerConstants() {
    }
}
