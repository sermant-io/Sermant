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

package com.huawei.flowcontrol.res4j.chain;

/**
 * 处理器常量
 *
 * @author zhouss
 * @since 2022-07-05
 */
public class HandlerConstants {
    /**
     * 监控优先级
     */
    public static final int MONITOR_ORDER = -2000;

    /**
     * 业务处理优先级
     */
    public static final int BUSINESS_ORDER = -1000;

    /**
     * 错误注入优先级
     */
    public static final int FAULT_ORDER = 3000;

    /**
     * 限流优先级
     */
    public static final int RATE_LIMIT_ORDER = 4000;

    /**
     * 隔离仓优先级
     */
    public static final int BULK_HEAD_ORDER = 5000;

    /**
     * 实例隔离优先级, 该优先级必须大于熔断优先级
     */
    public static final int INSTANCE_ISOLATION_ORDER = 9000;

    /**
     * 熔断优先级
     */
    public static final int CIRCUIT_BREAKER_ORDER = 10000;

    /**
     * 系统规则流控优先级
     */
    public static final int SYSTEM_RULE_FLOW_CONTROL = 11000;

    /**
     * 标记当前线程是否发生流控异常
     */
    public static final String OCCURRED_FLOW_EXCEPTION = "__OCCURRED_FLOW_EXCEPTION__";

    /**
     * 标记当前线程是否触发请求异常
     */
    public static final String OCCURRED_REQUEST_EXCEPTION = "__OCCURRED_REQUEST_EXCEPTION__";

    /**
     * 线程变量provider端key前缀
     */
    public static final String THREAD_LOCAL_DUBBO_PROVIDER_PREFIX = "PROVIDER:";

    /**
     * 线程变量consumer端key前缀
     */
    public static final String THREAD_LOCAL_DUBBO_CONSUMER_PREFIX = "CONSUMER:";

    /**
     * 键前缀
     */
    public static final String THREAD_LOCAL_KEY_PREFIX = HandlerConstants.class.getName()
            + "___THREAD_LOCAL_KEY_PREFIX___";

    private HandlerConstants() {
    }
}
