/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.spring.common.flowcontrol;

/**
 * 常量
 *
 * @author zhouss
 * @since 2022-07-28
 */
public class Constants {
    /**
     * 响应结果
     */
    public static final String HTTP_OK = "ok";

    /**
     * 熔断不触发异常的概率
     */
    public static final double BREAK_EXCEPTION_RATE = 0.05d;

    /**
     * 最大重试次数
     */
    public static final int RETRY_TIMES = 3;

    /**
     * 统一测试睡眠时间
     */
    public static final long SLEEP_TIME_MS = 100L;

    /**
     * 流控测试目标端, 分为服务端-provider，消费端-consumer
     */
    public static final String FLOW_CONTROL_TEST_TARGET_PROVIDER = "provider";

    /**
     * 流控测试目标端, 分为服务端-provider，消费端-consumer
     */
    public static final String FLOW_CONTROL_TEST_TARGET_CONSUMER = "consumer";

    /**
     * 目标端 - 环境变量名称
     */
    public static final String FLOW_CONTROL_TEST_TARGET_ENV = "sermant.flowcontrol.test.target";

    private Constants() {
    }
}
