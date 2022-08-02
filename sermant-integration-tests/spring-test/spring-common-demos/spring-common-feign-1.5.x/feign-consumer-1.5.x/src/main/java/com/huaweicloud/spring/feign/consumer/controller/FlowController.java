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

package com.huaweicloud.spring.feign.consumer.controller;

import com.huaweicloud.spring.common.flowcontrol.YamlSourceFactory;
import com.huaweicloud.spring.feign.api.FlowControlService;

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
    private FlowControlService flowControlService;

    /**
     * 实例隔离接口测试
     *
     * @return 实例隔离
     */
    @RequestMapping("instanceIsolation")
    public String instanceIsolation() {
        try {
            return flowControlService.instanceIsolation();
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
        Integer tryCount = null;
        try {
            tryCount = flowControlService.retry(UUID.randomUUID().toString());
        } catch (Exception ex) {
            LOGGER.error("Retry {} times", tryCount);
            LOGGER.error(ex.getMessage(), ex);
        }
        return tryCount == null ? 0 : tryCount;
    }

    /**
     * 限流测试
     *
     * @return ok
     */
    @RequestMapping("rateLimiting")
    public String rateLimiting() {
        return flowControlService.rateLimiting();
    }

    /**
     * 慢调用熔断测试
     *
     * @return ok
     */
    @RequestMapping("timedBreaker")
    public String timedBreaker() {
        try {
            return flowControlService.timedBreaker();
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
            return flowControlService.exceptionBreaker();
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
        return flowControlService.bulkhead();
    }

    /**
     * 请求头匹配测试
     *
     * @return ok
     */
    @RequestMapping("header")
    public String header() {
        try {
            return flowControlService.header();
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
            return flowControlService.serviceNameMatch();
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
        return flowControlService.serviceNameNoMatch();
    }

    private String convertMsg(Exception ex) {
        if (ex instanceof UndeclaredThrowableException) {
            return ((UndeclaredThrowableException) ex).getUndeclaredThrowable().getMessage();
        }
        return ex.getMessage();
    }
}
