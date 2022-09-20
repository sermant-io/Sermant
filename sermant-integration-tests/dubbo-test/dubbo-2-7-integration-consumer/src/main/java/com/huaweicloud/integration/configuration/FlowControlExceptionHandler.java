/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.integration.configuration;

import com.huaweicloud.integration.controller.FlowController;
import com.huaweicloud.integration.controller.FlowHeaderController;

import org.apache.dubbo.rpc.RpcException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 流控全局异常处理
 *
 * @author zhouss
 * @since 2022-09-16
 */
@ControllerAdvice(assignableTypes = {FlowController.class, FlowHeaderController.class})
public class FlowControlExceptionHandler {
    /**
     * 异常处理
     *
     * @param exception 异常
     * @return 异常结果
     */
    @ExceptionHandler(RpcException.class)
    public ResponseEntity<String> handleError(RpcException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.OK);
    }
}
