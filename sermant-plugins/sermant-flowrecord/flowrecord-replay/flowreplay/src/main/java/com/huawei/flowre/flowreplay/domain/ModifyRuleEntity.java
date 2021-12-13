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

package com.huawei.flowre.flowreplay.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * 回放数据入参修改规则
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-04-19
 */
@Getter
@Setter
public class ModifyRuleEntity {
    /**
     * 流量修改类型，仅支持“Concrete”和“Regex”
     */
    String type;

    /**
     * 流量修改的具体查找值或正则表达式
     */
    String search;

    /**
     * 流量修改的替换值
     */
    String replacement;
}
