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

package com.huawei.hercules.service.agent;

import com.huawei.hercules.config.FeignRequestInterceptor;
import com.huawei.hercules.fallback.AgentDownLoadServiceFallbackFactory;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 功能描述：agent下载相关接口
 *
 * 
 * @since 2021-10-14
 */
@FeignClient(
        url = "${controller.engine.url}" + "/rest/agent",
        name = "agentDownload",
        fallbackFactory = AgentDownLoadServiceFallbackFactory.class,
        configuration = FeignRequestInterceptor.class)
public interface IAgentDownloadService {
    /**
     * Download agent.
     *
     * @param fileName file path of agent
     */
    @RequestMapping(value = "/download/{fileName:[a-zA-Z0-9\\.\\-_]+}")
    Response download(@PathVariable String fileName);


    /**
     * Download the latest agent.
     *
     * @param owner   agent owner
     * @param region  agent region
     */
    @RequestMapping(value = "/download/{region}/{owner}")
    String downloadDirect(@PathVariable(value = "owner") String owner,
                          @PathVariable(value = "region") String region) ;

    /**
     * Download the latest agent.
     *
     * @param owner   agent owner
     * @param region  agent region
     */
    @RequestMapping(value = "/download")
    String download(@RequestParam(value = "owner", required = false) String owner,
                    @RequestParam(value = "region", required = false) String region);
}
