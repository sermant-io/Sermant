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

package com.huawei.common.constant;

import lombok.Getter;

/**
 * 主机状态枚举
 *
 * @author y30010171
 * @since 2021-12-09
 **/
@Getter
public enum ServerStatus {
    PENDING("pending","准备中"),
    RUNNING("running","运行中"),
    SUCCESS("success","成功"),
    FAIL("fail","失败");

    private String value;
    private String description;

    ServerStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
