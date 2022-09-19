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

package com.huawei.flowcontrol.common.context;

/**
 * 流控上下文, 当前仅用于标记是否触发流控规则
 *
 * @author zhouss
 * @since 2022-09-13
 */
public enum FlowControlContext {
    /**
     * 单例
     */
    INSTANCE;

    private final ThreadLocal<Boolean> isFlowControl = new ThreadLocal<>();

    /**
     * 触发流控
     */
    public void triggerFlowControl() {
        isFlowControl.set(Boolean.TRUE);
    }

    /**
     * 清理
     */
    public void clear() {
        isFlowControl.remove();
    }

    /**
     * 是否触发流控
     *
     * @return 是否触发流控
     */
    public boolean isFlowControl() {
        return isFlowControl.get() != null;
    }
}
