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

package com.huawei.registry.entity;

/**
 * The result is returned in front
 *
 * @author zhouss
 * @since 2022-02-16
 */
public class FixedResult {
    /**
     * Override the results
     */
    private Object result;

    /**
     * Whether the call needs to be skipped
     */
    private boolean isSkip = false;

    public Object getResult() {
        return result;
    }

    /**
     * Set the result
     *
     * @param result 结果
     */
    public void setResult(Object result) {
        this.result = result;
        this.isSkip = true;
    }

    public boolean isSkip() {
        return isSkip;
    }

    public void setSkip(boolean isNeedSkip) {
        this.isSkip = isNeedSkip;
    }
}
