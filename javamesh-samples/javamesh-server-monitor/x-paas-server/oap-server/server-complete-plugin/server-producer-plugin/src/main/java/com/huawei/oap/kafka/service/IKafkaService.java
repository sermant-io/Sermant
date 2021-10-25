/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.oap.kafka.service;

import org.apache.skywalking.apm.network.language.agent.v3.SegmentObject;
import org.apache.skywalking.oap.server.library.module.Service;

/**
 * @author hefan
 * @since 2021-06-21
 */
public interface IKafkaService extends Service {
    /**
     * 发送数据
     * @param segmentObject 记录
     */
    void send(SegmentObject segmentObject);
}
