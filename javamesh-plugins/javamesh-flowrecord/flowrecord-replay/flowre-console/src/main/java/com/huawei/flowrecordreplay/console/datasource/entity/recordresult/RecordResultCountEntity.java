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

package com.huawei.flowrecordreplay.console.datasource.entity.recordresult;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 录制流量总接口统计
 *
 * @author lihongjiang
 * @version 0.0.1
 * @since 2021-06-10
 */
@Getter
@Setter
public class RecordResultCountEntity {
    /**
     * 录制任务的id
     */
    private String jobId;

    /**
     * 录制总数目
     */
    private long recordTotalCount;

    /**
     * 录制接口统计
     */
    private List<RecordInterfaceCountEntity> recordInterfaceCount;
}
