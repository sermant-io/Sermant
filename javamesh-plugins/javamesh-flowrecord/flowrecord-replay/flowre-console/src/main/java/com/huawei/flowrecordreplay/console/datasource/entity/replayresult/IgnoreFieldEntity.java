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

package com.huawei.flowrecordreplay.console.datasource.entity.replayresult;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 接口忽略字段实体
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-04-07
 */
@Getter
@Setter
public class IgnoreFieldEntity {
    /**
     * 忽略字段的接口名
     */
    private String method;

    /**
     * 忽略字段相信信息列表
     */
    private List<Field> fields;
}

/**
 * 单独的一个字段
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-04-07
 */
@Getter
@Setter
class Field {
    /**
     * 接口下的字段名
     */
    private String name;

    /**
     * 字段的忽略情况
     */
    private boolean ignore;
}
