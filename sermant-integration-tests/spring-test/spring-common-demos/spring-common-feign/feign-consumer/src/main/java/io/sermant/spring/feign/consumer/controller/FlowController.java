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

package io.sermant.spring.feign.consumer.controller;

import io.sermant.spring.common.flowcontrol.YamlSourceFactory;
import io.sermant.spring.feign.api.FeignService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.UUID;

/**
 * 流控测试
 *
 * @author zhouss
 * @since 2022-07-29
 */
@Controller
@ResponseBody
@RequestMapping("flowcontrol")
@PropertySource(value = "classpath:rule.yaml", factory = YamlSourceFactory.class)
public class FlowController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlowController.class);

    @Autowired
    private FeignService feignService;

    /**
     * 实例隔离接口测试
     *
     * @return 实例隔离
     */
    @RequestMapping("instanceIsolation")
    public String instanceIsolation() {
        try {
            return feignService.instanceIsolation();
        } catch (Exception ex) {
            return convertMsg(ex);
        }
    }

    /**
     * 实例隔离接口测试
     *
     * @return 实例隔离
     */
    @RequestMapping("retry")
    public int retry() {
        String tryCount = null;
        try {
            tryCount = feignService.retry(UUID.randomUUID().toString());
        } catch (Exception ex) {
            LOGGER.error("Retry {} times", tryCount);
            LOGGER.error(ex.getMessage(), ex);
        }
        return tryCount == null ? 0 : Integer.parseInt(tryCount);
    }

    /**
     * 限流测试
     *
     * @return ok
     */
    @RequestMapping("rateLimiting")
    public String rateLimiting() {
        return feignService.rateLimiting();
    }

    /**
     * 限流测试
     *
     * @return ok
     */
    @RequestMapping("prefixRateLimiting")
    public String rateLimitingPrefix() {
        return rateLimiting();
    }

    /**
     * 限流测试
     *
     * @return ok
     */
    @RequestMapping("rateLimitingContains")
    public String rateLimitingContains() {
        return rateLimiting();
    }

    /**
     * 限流测试
     *
     * @return ok
     */
    @RequestMapping("rateLimitingSuffix")
    public String rateLimitingSuffix() {
        return rateLimiting();
    }

    /**
     * 慢调用熔断测试
     *
     * @return ok
     */
    @RequestMapping("timedBreaker")
    public String timedBreaker() {
        try {
            return feignService.timedBreaker();
        } catch (Exception ex) {
            return convertMsg(ex);
        }
    }

    /**
     * 异常熔断测试
     *
     * @return ok
     */
    @RequestMapping("exceptionBreaker")
    public String exceptionBreaker() {
        try {
            return feignService.exceptionBreaker();
        } catch (Exception ex) {
            return convertMsg(ex);
        }
    }

    /**
     * 隔离仓测试
     *
     * @return ok
     */
    @RequestMapping("bulkhead")
    public String bulkhead() {
        return feignService.bulkhead();
    }

    /**
     * 请求头匹配测试
     *
     * @return ok
     */
    @RequestMapping("header")
    public String header() {
        try {
            return feignService.headerExact();
        } catch (Exception ex) {
            return convertMsg(ex);
        }
    }

    /**
     * 请求头匹配测试
     *
     * @return ok
     */
    @RequestMapping("headerPrefix")
    public String headerPrefix() {
        try {
            return feignService.headerPrefix();
        } catch (Exception ex) {
            return convertMsg(ex);
        }
    }

    /**
     * 请求头匹配测试
     *
     * @return ok
     */
    @RequestMapping("headerSuffix")
    public String headerSuffix() {
        try {
            return feignService.headerSuffix();
        } catch (Exception ex) {
            return convertMsg(ex);
        }
    }

    /**
     * 请求头匹配测试
     *
     * @return ok
     */
    @RequestMapping("headerContains")
    public String headerContains() {
        try {
            return feignService.headerContains();
        } catch (Exception ex) {
            return convertMsg(ex);
        }
    }

    /**
     * 请求头匹配测试
     *
     * @return ok
     */
    @RequestMapping("headerCompareMatch")
    public String headerCompareMatch() {
        try {
            return feignService.headerCompareMatch();
        } catch (Exception ex) {
            return convertMsg(ex);
        }
    }

    /**
     * 请求头匹配测试
     *
     * @return ok
     */
    @RequestMapping("headerCompareNotMatch")
    public String headerCompareNotMatch() {
        try {
            return feignService.headerCompareNotMatch();
        } catch (Exception ex) {
            return convertMsg(ex);
        }
    }

    /**
     * 匹配服务名测试-匹配前提, 触发流控
     *
     * @return ok
     */
    @RequestMapping("serviceNameMatch")
    public String serviceNameMatch() {
        try {
            return feignService.serviceNameMatch();
        } catch (Exception ex) {
            return convertMsg(ex);
        }
    }

    /**
     * 匹配服务名测试-不匹配前提, 不触发流控
     *
     * @return ok
     */
    @RequestMapping("serviceNameNoMatch")
    public String serviceNameNoMatch() {
        return feignService.serviceNameNoMatch();
    }

    /**
     * 错误注入测试-返回空
     *
     * @return 返回空-由agent实现
     */
    @RequestMapping("faultNull")
    public String faultNull() {
        return feignService.faultNull();
    }

    /**
     * 错误注入测试-抛异常
     *
     * @return 抛异常-由agent实现
     */
    @RequestMapping("faultThrow")
    public String faultThrow() {
        try {
            feignService.faultThrow();
        } catch (Exception ex) {
            return convertMsg(ex);
        }
        return "";
    }

    /**
     * 错误注入测试-请求延迟
     *
     * @return 请求延迟-由agent实现
     */
    @RequestMapping("faultDelay")
    public String faultDelay() {
        return feignService.faultDelay();
    }

    private String convertMsg(Exception ex) {
        if (ex instanceof UndeclaredThrowableException) {
            return ((UndeclaredThrowableException) ex).getUndeclaredThrowable().getMessage();
        }
        return ex.getMessage();
    }
}
