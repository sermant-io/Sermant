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

package com.huawei.flowre.flowreplay.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 回放节点的一系列指标
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-07-19
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FlowReplayMetric {
    /**
     * 回放节点名称
     */
    private String replayWorkerName;

    /**
     * 回放任务的ID
     */
    private String replayJobId;

    /**
     * 时间戳 (s)
     */
    private long timeStamp;

    /**
     * 回放的RPS
     */
    private int rps;

    /**
     * 线程数
     */
    private int threadCount;
}
