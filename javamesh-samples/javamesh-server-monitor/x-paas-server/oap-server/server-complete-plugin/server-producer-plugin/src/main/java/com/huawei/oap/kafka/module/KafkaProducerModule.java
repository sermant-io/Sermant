/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.oap.kafka.module;

import com.huawei.oap.kafka.service.IKafkaService;
import org.apache.skywalking.oap.server.library.module.ModuleDefine;

/**
 * kafka生产者模块
 *
 * @author hefan
 * @since 2021-06-21
 */
public class KafkaProducerModule extends ModuleDefine {
    /**
     * 模块名
     */
    public static final String NAME = "kafka-complement-producer";

    public KafkaProducerModule() {
        super(NAME);
    }

    @Override
    public Class[] services() {
        return new Class[]{
            IKafkaService.class
        };
    }
}
