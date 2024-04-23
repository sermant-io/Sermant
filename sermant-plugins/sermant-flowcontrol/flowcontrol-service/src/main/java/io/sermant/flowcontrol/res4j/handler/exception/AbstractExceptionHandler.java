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

package io.sermant.flowcontrol.res4j.handler.exception;

import io.sermant.flowcontrol.common.entity.FlowControlResponse;
import io.sermant.flowcontrol.common.entity.FlowControlResult;
import io.sermant.flowcontrol.res4j.util.SerializeUtils;

import java.util.Locale;
import java.util.Optional;

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
        if (response.isReplaceResult()) {
            final Optional<String> serializeResult = SerializeUtils.serialize2String(response.getResult());
            if (serializeResult.isPresent()) {
                response.setSerializeResult(serializeResult.get());
            } else {
                response.setSerializeResult(String.format(Locale.ENGLISH,
                        "Can not serialize target class [%s]",
                        response.getResult() == null ? "null result" : response.getResult().getClass().getName()));
            }
        }
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
