/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import com.huawei.javamesh.metricserver.dto.register.AgentRegistrationDTO;
import com.huawei.javamesh.metricserver.dto.register.NetworkAddressDTO;
import com.huawei.javamesh.metricserver.service.AgentRegistrationService;
import com.huawei.javamesh.sample.servermonitor.entity.AgentRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Agent注册信息 kafka接收处理类
 */
@Component
public class AgentRegistrationKafkaReceiver {

    private final AgentRegistrationService registrationService;

    @Autowired
    public AgentRegistrationKafkaReceiver(AgentRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @KafkaListener(topics = "topic-agent-registration", groupId = "monitor-server")
    public void onMessage(byte[] record) {
        final AgentRegistration registration;
        try {
            registration = AgentRegistration.parseFrom(record);
        } catch (InvalidProtocolBufferException e) {
            return;
        }
        AgentRegistrationDTO registrationDTO = AgentRegistrationDTO.builder()
            .service(registration.getService())
            .serviceInstance(registration.getServiceInstance())
            .jvmVendor(registration.getJvmVendor())
            .jvmVersion(registration.getJvmVersion())
            .runtimeVersion(registration.getRuntimeVersion())
            .networkAddresses(registration.getNetworkAddressesList().stream()
                .map(networkAddress -> NetworkAddressDTO.builder()
                    .hostname(networkAddress.getHostname())
                    .address(networkAddress.getAddress())
                    .build()).collect(Collectors.toList()))
            .build();
        registrationService.register(registrationDTO);
    }
}
