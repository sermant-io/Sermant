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

package com.huawei.flowre.flowreplay.domain.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * 回放结果发送到kafka的消息
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-06-30
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplayResultMessage {
    /**
     * 流量的Trace Id
     */
    private String traceId;

    /**
     * 回放任务的Id 存放回放结果时
     */
    private String replayJobId;

    /**
     * 当前回放接口
     */
    private String methodName;

    /**
     * 接口响应状态
     */
    private int statusCode;

    /**
     * 流量录制时返回值
     */
    private String responseBody;

    /**
     * 流量回放时返回值
     */
    private String replayResult;

    /**
     * 流量录制时间
     */
    private Date recordTime;

    /**
     * 流量回放时间
     */
    private Date replayTime;

    /**
     * 响应时间
     */
    private long responseTime;
}
