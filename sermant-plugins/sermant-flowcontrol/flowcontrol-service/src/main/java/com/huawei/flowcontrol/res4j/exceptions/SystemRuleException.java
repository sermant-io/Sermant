/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.flowcontrol.res4j.exceptions;

import com.huawei.flowcontrol.common.core.rule.SystemRule;

/**
 * System flow control exception
 *
 * @author xuezechao1
 * @since 2022-12-06
 */
public class SystemRuleException extends RuntimeException {
    private final String msg;

    private final SystemRule systemRule;

    /**
     * system policy exception
     *
     * @param msg exception message
     * @param systemRule system rule
     */
    public SystemRuleException(String msg, SystemRule systemRule) {
        this.msg = msg;
        this.systemRule = systemRule;
    }

    public String getMsg() {
        return msg;
    }

    public SystemRule getSystemRule() {
        return systemRule;
    }
}
