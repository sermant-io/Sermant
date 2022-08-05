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

package com.huawei.flowcontrol.common.core.constants;

/**
 * 规则常量
 *
 * @author zhouss
 * @since 2022-08-05
 */
public class RuleConstants {
    /**
     * 错误注入规则类型-延迟
     */
    public static final String FAULT_RULE_DELAY_TYPE = "delay";

    /**
     * 错误注入规则类型-丢弃
     */
    public static final String FAULT_RULE_ABORT_TYPE = "abort";

    /**
     * 错误注入回调类型-抛出异常
     */
    public static final String FAULT_RULE_FALLBACK_THROW_TYPE = "ThrowException";

    /**
     * 错误注入回调类型-返回空
     */
    public static final String FAULT_RULE_FALLBACK_NULL_TYPE = "ReturnNull";

    private RuleConstants() {
    }
}
