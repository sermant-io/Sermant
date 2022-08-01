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

package com.huaweicloud.flowcontrol.rest.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;

/**
 * 流控测试
 *
 * @author zhouss
 * @since 2022-08-01
 */
@Controller
@ResponseBody
public class FlowRestController {
    private static final long DEFAULT_SLEEP_MS = 150L;

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowRestController.class);

    private static final int GAP = 100;

    @Value("${server.port}")
    int port;

    /**
     * 流控测试接口
     *
     * @param exRate 异常率
     * @return ok
     * @throws Exception 抛出异常
     */
    @RequestMapping(value = "/flow", method = RequestMethod.GET)
    public String flow(@RequestParam(required = false) Integer exRate) throws Exception {
        Thread.sleep(DEFAULT_SLEEP_MS);
        LOGGER.info("retry port:{} --{}", port, LocalDateTime.now());
        if (exRate != null && GAP - exRate < Math.random() * GAP) {
            throw new Exception("need retry");
        }
        return "Hello, I am zk rest template provider, my port is " + port;
    }
}
