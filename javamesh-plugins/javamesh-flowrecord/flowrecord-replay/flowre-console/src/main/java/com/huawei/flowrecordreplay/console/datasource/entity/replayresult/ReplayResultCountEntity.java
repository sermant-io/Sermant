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
import java.util.Map;

/**
 * 流量回放结果统计返回体
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-04-06
 */
@Getter
@Setter
public class ReplayResultCountEntity {
    /**
     * 回放任务的id
     */
    private String jobId;

    /**
     * 回放成功的数目
     */
    private long replaySuccessCount;

    /**
     * 回放失败的数目
     */
    private long replayFailureCount;

    /**
     * 响应结果统计数据
     */
    private Map<String, Long> statusCodeStatistics;

    /**
     * 总的回放数目
     */
    private long replayTotal;

    /**
     * 回放接口统计
     */
    private List<ReplayInterfaceCountEntity> replayInterfaceCount;

    /**
     * 重新比对状态
     */
    private boolean reCompareStatus = false;
}