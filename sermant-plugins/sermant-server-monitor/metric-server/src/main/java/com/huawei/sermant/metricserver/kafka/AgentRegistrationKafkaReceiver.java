/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.metricserver.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import com.huawei.sermant.metricserver.dto.register.AgentRegistrationDTO;
import com.huawei.sermant.metricserver.dto.register.NetworkAddressDTO;
import com.huawei.sermant.metricserver.service.AgentRegistrationService;
import com.huawei.sermant.plugin.servermonitor.entity.AgentRegistration;
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
