package com.huawei.apm.backend.service;

import com.huawei.apm.backend.common.conf.KafkaConf;
import org.springframework.stereotype.Component;

@Component
public interface SendService {
    void send(KafkaConf conf, String str);
}
