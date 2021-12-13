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

package com.huawei.flowrecordreplay.console.datasource.entity.replayresult;

import com.alibaba.fastjson.JSONObject;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 流量回放结果概览
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-04-07
 */
@Getter
@Setter
public class ReplayResultCompareEntity {
    /**
     * 回放结果列表
     */
    private List<JSONObject> replayResultCompareEntityList;

    /**
     * 回放总数
     */
    private long total;
}
