/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.spring.feign.provider;

import io.sermant.spring.feign.api.RemovalBootService;
import io.sermant.spring.feign.api.RemovalService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 离群实例摘除插件的feign测试
 *
 * @author zhp
 * @since 2023-03-16
 */
@Controller
@ResponseBody
public class RemovalServiceImpl implements RemovalService, RemovalBootService {
    @Value("${server.port:8099}")
    private int port;

    /**
     * 超时时间
     */
    @Value("${timeout}")
    private int timeout;

    /**
     * 离群实例摘除的服务调用
     *
     * @return ok
     * @throws RuntimeException 运行异常
     */
    @Override
    @RequestMapping("/testRemoval")
    public String testRemoval() {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return String.valueOf(port);
    }
}