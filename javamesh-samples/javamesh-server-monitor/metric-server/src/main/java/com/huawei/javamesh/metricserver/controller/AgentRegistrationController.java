/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
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
