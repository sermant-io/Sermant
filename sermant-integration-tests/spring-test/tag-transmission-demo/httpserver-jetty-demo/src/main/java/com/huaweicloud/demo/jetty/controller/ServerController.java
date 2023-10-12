/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.demo.jetty.controller;

import com.huaweicloud.demo.lib.utils.HttpClientUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * http Server端，供http客户端调用
 *
 * @author daizhenyu
 * @since 2023-09-07
 **/
@Controller
@ResponseBody
@RequestMapping(value = "jetty")
public class ServerController {
    @Value("${apacheDubboUrl}")
    private String apacheDubboUrl;

    /**
     * 验证jetty服务端透传流量标签
     *
     * @return 流量标签值
     */
    @RequestMapping(value = "httpserver", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String testJetty() {
        return HttpClientUtils.doHttpClientV4Get(apacheDubboUrl);
    }
}
