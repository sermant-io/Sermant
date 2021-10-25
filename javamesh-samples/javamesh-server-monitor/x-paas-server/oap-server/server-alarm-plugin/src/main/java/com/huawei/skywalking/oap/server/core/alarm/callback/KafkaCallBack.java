/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.skywalking.oap.server.core.alarm.callback;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.skywalking.oap.server.core.alarm.AlarmCallback;
import org.apache.skywalking.oap.server.core.alarm.AlarmMessage;
import org.apache.skywalking.oap.server.core.alarm.provider.AlarmSettings;

import java.util.List;

/**
 * 发送告警信息到kafka
 *
 * @author hudeyu
 * @since 2021-07-27
 */
@Slf4j
public class KafkaCallBack implements AlarmCallback {
    private AlarmSettings alarmSettings;
    private KafkaProducer<String, String> kafkaProducer;
    private Gson gson = new Gson();

    public KafkaCallBack(KafkaProducer<String, String> kafkaProducer,
                         AlarmSettings alarmSettings) {
        this.kafkaProducer = kafkaProducer;
        this.alarmSettings = alarmSettings;
    }

    /**
     * 发送告警回调函数
     *
     * @param alarmMessage 告警信息
     */
    @Override
    public void doAlarm(List<AlarmMessage> alarmMessage) {
        try {
            for (AlarmMessage message : alarmMessage) {
                kafkaProducer.send(new ProducerRecord<String, String>(alarmSettings.getTopic(),
                                "AlarmMessage", gson.toJson(message)),
                        (metadata, exception) -> {
                            if (exception != null) {
                                log.error("[KafkaCallBack] Kafka exception in " + alarmSettings.getTopic()
                                        + exception.getMessage());
                            }
                        });
                log.debug("Send alarmMessage to kafka success!");
            }
        } catch (KafkaException e) {
            log.error("[KafkaCallBack] Send alarmMessage to kafka exception in " + alarmSettings.getTopic()
                    + e.getMessage());
        }
    }
}
