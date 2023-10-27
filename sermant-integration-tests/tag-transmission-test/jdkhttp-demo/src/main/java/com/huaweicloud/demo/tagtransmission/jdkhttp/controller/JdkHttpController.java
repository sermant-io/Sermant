/*
 *   Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.huaweicloud.demo.tagtransmission.jdkhttp.controller;

import com.huaweicloud.demo.tagtransmission.util.HttpClientUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * controller，使用JdkHttp调用http 服务端
 *
 * @author daizhenyu
 * @since 2023-10-14
 **/
@RestController
@RequestMapping(value = "jdkHttp")
public class JdkHttpController {
    @Value("${jetty.server.url}")
    private String jettyServerUrl;

    @Value("${tomcat.server.url}")
    private String tomcatServerUrl;

    /**
     * 验证JdkHttp透传流量标签, 调用jetty服务端
     *
     * @return 流量标签值
     * @throws IOException
     */
    @RequestMapping(value = "testJdkHttpAndJetty", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String testJdkHttpAndJetty() {
        return HttpClientUtils.doHttpUrlConnectionGet(jettyServerUrl);
    }

    /**
     * 验证JdkHttp透传流量标签，调用tomcat服务端
     *
     * @return 流量标签值
     * @throws IOException
     */
    @RequestMapping(value = "testJdkHttpAndTomcat", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String testJdkHttpAndTomcat() {
        return HttpClientUtils.doHttpUrlConnectionGet(tomcatServerUrl);
    }
}
