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

package com.huawei.flowre.mockserver.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * 从数据库获得的原始response中的关键信息(返回值类型名，返回值序列化字符串等)
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-02-22
 */
@Getter
@Setter
public class SelectResult {
    /**
     * 子调用返回结果 序列化后的字符串
     */
    private String selectContent;

    /**
     * 子调用返回结果的类型
     */
    private String selectClassName;
}
