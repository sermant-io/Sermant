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

package com.huawei.nacos.rest.data;

import com.huawei.nacos.common.HostUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

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

    private String ip;

    /**
     * 请求方法
     *
     * @param request 请求信息
     * @return String
     */
    @RequestMapping("/graceDown")
    public String graceDown(HttpServletRequest request) {
        if (ip == null) {
            ip = HostUtils.getMachineIp();
        }
        return String.format(Locale.ENGLISH, "%s[%s:%s]", serviceName, ip, request.getLocalPort());
    }
}
