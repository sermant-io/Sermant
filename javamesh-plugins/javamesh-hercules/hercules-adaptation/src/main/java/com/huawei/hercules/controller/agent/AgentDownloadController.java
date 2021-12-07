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

package com.huawei.hercules.controller.agent;

import com.alibaba.fastjson.JSONObject;
import com.huawei.hercules.controller.agent.constant.ResponseColumn;
import com.huawei.hercules.exception.HerculesException;
import com.huawei.hercules.service.agent.IAgentDownloadService;
import com.huawei.hercules.util.DownloadUtils;
import feign.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 功能描述：下载agent
 *
 * 
 * @since 2021-10-14
 */
@RestController
@RequestMapping("/api/agent")
public class AgentDownloadController {
    /**
     * 定义日志工具
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentDownloadController.class);

    @Autowired
    private IAgentDownloadService agentDownloadService;

    /**
     * Download agent.
     *
     * @param fileName file path of agent
     */
    @RequestMapping(value = "/download/{fileName:[a-zA-Z0-9.\\-_]+}", method = RequestMethod.GET)
    void download(@PathVariable String fileName, HttpServletResponse httpServletResponse) {
        Response response = agentDownloadService.download(fileName);
        if (response == null) {
            LOGGER.error("The response of downloading from feign is null, fileName=[{}].", fileName);
            throw new HerculesException("The response of downloading from feign is null.");
        }
        Response.Body body = response.body();
        if (body == null) {
            LOGGER.error("The body of response from feign is null, fileName=[{}].", fileName);
            throw new HerculesException("The body of response from feign is null.");
        }
        try (InputStream inputStream = body.asInputStream()) {
            boolean downloadFileSuccess = DownloadUtils.downloadFile(httpServletResponse, inputStream, fileName);
            if (!downloadFileSuccess) {
                throw new HerculesException("Download agent package fail.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Download agent package name.
     */
    @RequestMapping(value = "/link", method = RequestMethod.GET)
    JSONObject downloadUrl(@RequestParam(value = "owner", required = false) String owner,
                           @RequestParam(value = "region", required = false) String region) {
        String packageName = agentDownloadService.download(owner, region);
        LOGGER.info("Get the package name: {}.", packageName);
        JSONObject jsonObject = new JSONObject();
        Map<String, String> link = new HashMap<>();
        link.put("link", packageName);
        jsonObject.put(ResponseColumn.RESPONSE_DATA_ELEMENT, link);
        return jsonObject;
    }
}
