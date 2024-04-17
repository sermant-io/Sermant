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
 * regular constant
 *
 * @author zhouss
 * @since 2022-08-05
 */
public class RuleConstants {
    /**
     * error injection rule type-delay
     */
    public static final String FAULT_RULE_DELAY_TYPE = "delay";

    /**
     * error injection rule type-discard
     */
    public static final String FAULT_RULE_ABORT_TYPE = "abort";

    /**
     * error injection callback type-throwException
     */
    public static final String FAULT_RULE_FALLBACK_THROW_TYPE = "ThrowException";

    /**
     * error injection callback type-returnNull
     */
    public static final String FAULT_RULE_FALLBACK_NULL_TYPE = "ReturnNull";

    private RuleConstants() {
    }
}
