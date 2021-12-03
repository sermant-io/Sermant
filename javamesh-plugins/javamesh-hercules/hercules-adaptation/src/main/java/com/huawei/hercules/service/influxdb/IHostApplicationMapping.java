/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.hercules.service.influxdb;

import com.huawei.hercules.config.FeignRequestInterceptor;
import com.huawei.hercules.controller.monitor.dto.AgentRegistrationDTO;
import com.huawei.hercules.fallback.HostAppMappingFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 功能描述：主机信息和应用部署服务实例之间的映射
 *
 * @author z30009938
 * @since 2021-11-22
 */
@FeignClient(url = "${monitor.agent.url}",
        name = "monitor",
        fallbackFactory = HostAppMappingFallbackFactory.class,
        configuration = FeignRequestInterceptor.class
)
public interface IHostApplicationMapping {
    /**
     * 查询host和application之间的映射关系
     *
     * @param hostname 主机名称
     * @return 映射关系
     */
    @GetMapping("/registrations/{hostname}")
    List<AgentRegistrationDTO> getRegistrationsByHostname(@PathVariable("hostname") String hostname);
}
