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

package com.huaweicloud.spring.feign.api;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 服务端流控测试
 *
 * @author zhouss
 * @since 2022-07-29
 */
@FeignClient(name = "feign-provider", configuration = HeaderMatchConfiguration.class)
public interface FlowControlService {
    /**
     * 限流测试
     *
     * @return ok
     */
    @RequestMapping("rateLimiting")
    String rateLimiting();

    /**
     * 慢调用熔断测试
     *
     * @return ok
     */
    @RequestMapping("timedBreaker")
    String timedBreaker();

    /**
     * 异常熔断测试
     *
     * @return ok
     * @throws Exception 模拟异常率
     */
    @RequestMapping("exceptionBreaker")
    String exceptionBreaker() throws Exception;

    /**
     * 隔离仓测试
     *
     * @return ok
     */
    @RequestMapping("bulkhead")
    String bulkhead();

    /**
     * 请求头匹配测试
     *
     * @return ok
     */
    @RequestMapping("header")
    String header();

    /**
     * 匹配服务名测试-匹配前提, 触发流控
     *
     * @return ok
     */
    @RequestMapping("serviceNameMatch")
    String serviceNameMatch();

    /**
     * 匹配服务名测试-不匹配前提, 不触发流控
     *
     * @return ok
     */
    @RequestMapping("serviceNameNoMatch")
    String serviceNameNoMatch();

    /**
     * 实例隔离接口测试
     *
     * @return 实例隔离
     * @throws InterruptedException 线程中断抛出
     */
    @RequestMapping("instanceIsolation")
    String instanceIsolation() throws InterruptedException;

    /**
     * 实例隔离接口测试
     *
     * @param invocationId 调用ID
     * @return 实例隔离
     * @throws Exception 模拟异常重试
     */
    @RequestMapping("retry")
    int retry(@RequestParam("invocationId") String invocationId) throws Exception;
}
