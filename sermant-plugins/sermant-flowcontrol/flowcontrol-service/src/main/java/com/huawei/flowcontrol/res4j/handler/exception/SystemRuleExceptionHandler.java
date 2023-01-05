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

package com.huawei.flowcontrol.res4j.handler.exception;

import com.huawei.flowcontrol.common.entity.FlowControlResponse;
import com.huawei.flowcontrol.common.entity.FlowControlResult;
import com.huawei.flowcontrol.res4j.exceptions.SystemRuleException;

/**
 * 系统流控异常处理
 *
 * @author xuezechao1
 * @since 2022-12-06
 */
public class SystemRuleExceptionHandler extends AbstractExceptionHandler<SystemRuleException> {

    @Override
    protected FlowControlResponse getFlowControlResponse(SystemRuleException ex, FlowControlResult flowControlResult) {
        return new FlowControlResponse(ex.getMsg(), ex.getSystemRule().getErrorCode());
    }

    @Override
    public Class<SystemRuleException> targetException() {
        return SystemRuleException.class;
    }
}
