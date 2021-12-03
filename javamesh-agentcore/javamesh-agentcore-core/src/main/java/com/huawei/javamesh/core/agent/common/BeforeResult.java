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

package com.huawei.javamesh.core.agent.common;

/**
 * 实例和静态方法拦截器前置方法执行结果承载类
 */
public class BeforeResult {

    private boolean isContinue = true;

    private Object result;

    public void setResult(Object result) {
        this.result = result;
        isContinue = false;
    }

    public boolean isContinue() {
        return isContinue;
    }

    public Object getResult() {
        return result;
    }
}
