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

package com.huawei.flowcontrol.exception;

import com.huawei.javamesh.core.exception.BizException;

/**
 * 流控异常类
 * 拦截器抛出afterMethod方法抛出此类型异常将继续向上抛给调用方
 *
 * @author liyi
 * @since 1.0 2020-10-10
 */
public class FlowControlException extends BizException {
    /**
     * 通用自定义类构造方法
     *
     * @param msg 错误信息
     */
    public FlowControlException(String msg) {
        super(msg);
    }

    /**
     * 通用自定义类构造方法
     *
     * @param e 异常
     */
    public FlowControlException(Throwable e) {
        super(e);
    }

    /**
     * 通用自定义类构造方法
     *
     * @param msg 错误信息
     * @param e 异常
     */
    public FlowControlException(String msg, Throwable e) {
        super(msg, e);
    }

}
