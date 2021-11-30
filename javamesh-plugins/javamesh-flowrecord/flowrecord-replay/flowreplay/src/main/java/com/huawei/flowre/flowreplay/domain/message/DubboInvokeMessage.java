/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
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
