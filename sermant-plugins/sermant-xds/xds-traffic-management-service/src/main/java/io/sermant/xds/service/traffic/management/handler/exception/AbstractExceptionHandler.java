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

package io.sermant.xds.service.traffic.management.handler.exception;

import io.sermant.xds.common.entity.FlowControlResponse;
import io.sermant.xds.common.entity.FlowControlResult;

/**
 * abstract public exception handling
 *
 * @param <E> exceptionType
 * @author zhouss
 * @since 2022-08-08
 */
public abstract class AbstractExceptionHandler<E extends Throwable> implements ExceptionHandler<E> {
    @Override
    public void accept(E ex, FlowControlResult flowControlResult) {
        final FlowControlResponse response = getFlowControlResponse(ex, flowControlResult);
        flowControlResult.setResponse(response);
    }

    /**
     * get the flow control response
     *
     * @param flowControlResult flowControlResult
     * @param ex triggerException
     * @return flow control response
     */
    protected abstract FlowControlResponse getFlowControlResponse(E ex, FlowControlResult flowControlResult);
}
