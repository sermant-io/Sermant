/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol.common.enums;

/**
 * 流控拦截提示
 *
 * @author zhouss
 * @since 2022-01-24
 */
public enum FlowControlEnum {
    /**
     * 流控异常
     */
    RATE_LIMITED("Flow Limited"),

    /**
     * 熔断异常
     */
    CIRCUIT_BREAKER("Degraded and blocked"),

    /**
     * 隔离仓异常
     */
    BULKHEAD_FULL("Exceeded the max concurrent calls!");

    /**
     * 提示信息
     */
    private final String msg;

    FlowControlEnum(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
