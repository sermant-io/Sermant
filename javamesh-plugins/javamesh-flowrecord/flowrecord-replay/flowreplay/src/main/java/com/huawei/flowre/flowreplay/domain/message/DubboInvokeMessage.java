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

import com.huawei.flowre.flowreplay.config.Const;
import com.huawei.flowre.flowreplay.domain.content.DubboInvokeContent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * dubbo封装请求后放入kafka的数据
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-07-13
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DubboInvokeMessage {
    /**
     * 消息类型
     */
    String type = Const.DUBBO_TYPE;

    /**
     * 录制数据TraceId
     */
    String traceId;

    /**
     * 接口名
     */
    String methodName;

    /**
     * 回放任务id
     */
    String replayJobId;

    /**
     * 录制时返回值
     */
    String responseBody;

    /**
     * 录制时间
     */
    Date recordTime;

    /**
     * http请求封装实体
     */
    DubboInvokeContent httpInvokeContent;
}
