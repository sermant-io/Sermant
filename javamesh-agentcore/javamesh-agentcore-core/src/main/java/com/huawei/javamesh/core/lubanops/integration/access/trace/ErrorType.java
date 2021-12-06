/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.javamesh.core.lubanops.integration.access.trace;

/**
 * 调用链span错误类型， <br>
 *
 * @author
 * @since 2020年3月4日
 */
public enum ErrorType {

    /**
     * log.error 产生的异常
     */
    log,
    /**
     * 方法抛出的异常
     */
    method,
    /**
     * 状态码异常
     */
    statuscode,
    /**
     * 业务异常
     */
    bizcode,

    /**
     * 第三方传递
     */
    propagate
}
