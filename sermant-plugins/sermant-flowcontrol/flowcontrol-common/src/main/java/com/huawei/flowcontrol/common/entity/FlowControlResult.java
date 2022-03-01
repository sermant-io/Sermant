/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.common.entity;

import com.huawei.flowcontrol.common.enums.FlowControlEnum;

/**
 * 修正结果, 该结果确定返回数据
 *
 * @author zhouss
 * @since 2022-02-09
 */
public class FlowControlResult {
    /**
     * 流控类型
     */
    private FlowControlEnum flowControlEnum;

    /**
     * 是否需要跳过调用
     */
    private boolean isSkip = false;

    public FlowControlEnum getResult() {
        return flowControlEnum;
    }

    public void setResult(FlowControlEnum result) {
        this.flowControlEnum = result;
        this.isSkip = true;
    }

    public boolean isSkip() {
        return isSkip;
    }

    public void setSkip(boolean isNeedSkip) {
        this.isSkip = isNeedSkip;
    }
}
