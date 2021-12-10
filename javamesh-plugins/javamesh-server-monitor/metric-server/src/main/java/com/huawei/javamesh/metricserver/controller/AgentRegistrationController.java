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

package com.huawei.javamesh.metricserver.controller;

import com.huawei.javamesh.metricserver.dto.register.AgentRegistrationDTO;
import com.huawei.javamesh.metricserver.service.AgentRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Agent注册信息Controller
 */
@RestController
@RequestMapping("/agent")
public class AgentRegistrationController {

    private final AgentRegistrationService registrationService;

    @Autowired
    public AgentRegistrationController(AgentRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/registration/{service}/{instance}")
    public AgentRegistrationDTO getRegistration(@PathVariable("service") String service,
                                                @PathVariable("instance") String serviceInstance) {
        return registrationService.getRegistration(service, serviceInstance);
    }

    @GetMapping("/registrations/{hostname}")
    public List<AgentRegistrationDTO> getRegistrationsByHostname(@PathVariable("hostname") String hostname) {
        return registrationService.getRegistrationsByHostname(hostname);
    }

    @GetMapping("/registrations")
    public List<AgentRegistrationDTO> listRegistrations() {
        return registrationService.listRegistrations();
    }
}
