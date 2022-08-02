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

package com.huaweicloud.spring.common.flowcontrol.provider;

import com.huaweicloud.spring.common.flowcontrol.Constants;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 请求控制器
 *
 * @author zhouss
 * @since 2022-07-28
 */
@Controller
@ResponseBody
@RequestMapping
public class ProviderController {
    /**
     * 限流测试
     *
     * @return ok
     */
    @RequestMapping("rateLimiting")
    public String rateLimiting() {
        return Constants.HTTP_OK;
    }

    /**
     * 熔断测试(慢调用)
     *
     * @return ok
     */
    @RequestMapping("timedBreaker")
    public String timedBreaker() {
        try {
            Thread.sleep(Constants.SLEEP_TIME_MS);
        } catch (InterruptedException ignored) {
            // ignored
        }
        return Constants.HTTP_OK;
    }

    /**
     * 熔断测试(异常)
     *
     * @return ok
     * @throws Exception 一定概率抛出异常, 模拟异常
     */
    @RequestMapping("exceptionBreaker")
    public String exceptionBreaker() throws Exception {
        if (Math.random() > Constants.BREAK_EXCEPTION_RATE) {
            throw new Exception("Occurred error");
        }
        return Constants.HTTP_OK;
    }

    /**
     * 隔离仓测试
     *
     * @return ok
     */
    @RequestMapping("bulkhead")
    public String bulkhead() {
        try {
            Thread.sleep(Constants.SLEEP_TIME_MS);
        } catch (InterruptedException ignored) {
            // ignored
        }
        return Constants.HTTP_OK;
    }

    /**
     * 请求头匹配测试
     *
     * @return ok
     */
    @RequestMapping("header")
    public String header() {
        return Constants.HTTP_OK;
    }

    /**
     * 匹配服务名
     *
     * @return ok
     */
    @RequestMapping("serviceNameMatch")
    public String serviceNameMatch() {
        try {
            Thread.sleep(Constants.SLEEP_TIME_MS);
        } catch (InterruptedException ignored) {
            // ignored
        }
        return Constants.HTTP_OK;
    }

    /**
     * 不匹配服务名
     *
     * @return ok
     */
    @RequestMapping("serviceNameNoMatch")
    public String serviceNameNoMatch() {
        try {
            Thread.sleep(Constants.SLEEP_TIME_MS);
        } catch (InterruptedException ignored) {
            // ignored
        }
        return Constants.HTTP_OK;
    }
}
