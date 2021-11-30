package com.huawei.javamesh.backend.service;

import com.huawei.javamesh.backend.common.conf.KafkaConf;
import org.springframework.stereotype.Component;

@Component
public interface SendService {
    void send(KafkaConf conf, String str);
}
