/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.core.exception;

import com.huawei.sermant.core.agent.annotations.AboutDelete;

/**
 * 业务异常 抛出异常到调用方，即抛给用户应用
 * <p>
 * 可参考{@see com.huawei.flowcontrol.exception.FlowControlException}
 *
 * @author zhouss
 * @since 2021-11-12
 */
@AboutDelete // 如有需要，可自定义AdviceTemplate，关掉或修改suppress
@Deprecated
public class BizException extends RuntimeException {

    public BizException(String msg) {
        super(msg);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }

    public BizException(Throwable cause) {
        super(cause);
    }
}
