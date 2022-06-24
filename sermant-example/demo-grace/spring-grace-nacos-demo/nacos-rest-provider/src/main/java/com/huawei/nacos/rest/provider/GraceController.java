/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.nacos.rest.provider;

import com.huawei.nacos.common.HostUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 优雅上下线测试
 *
 * @author zhouss
 * @since 2022-06-16
 */
@RestController
public class GraceController {
    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${grace.providerName}")
    private String providerServiceName;

    @Autowired
    private RestTemplate restTemplate;

    private String ip;

    /**
     * 请求方法
     *
     * @param request 请求信息
     * @param response 响应信息
     * @return 结果
     */
    @RequestMapping("/graceDown")
    public String graceDown(HttpServletRequest request, HttpServletResponse response) {
        final String result = restTemplate
                .getForObject(String.format(Locale.ENGLISH, "http://%s/graceDown", providerServiceName),
                        String.class);
        if (ip == null) {
            ip = HostUtils.getMachineIp();
        }
        final String cur = String
                .format(Locale.ENGLISH, "%s[%s:%s]", serviceName, ip, request.getLocalPort());
        return cur + "->" + result;
    }
}
