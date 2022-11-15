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

import com.huaweicloud.spring.common.FeignConstants;
import com.huaweicloud.spring.feign.api.configuration.HeaderMatchConfiguration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * feign测试
 *
 * @author zhouss
 * @since 2022-07-29
 */
@FeignClient(qualifier = FeignConstants.FEIGN_SERVICE_BEAN_NAME, name = "feign-provider", configuration =
    HeaderMatchConfiguration.class)
public interface FeignService {
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
    String retry(@RequestParam("invocationId") String invocationId) throws Exception;

    /**
     * 错误注入测试-返回空
     *
     * @return 返回空-由agent实现
     */
    @RequestMapping("faultNull")
    String faultNull();

    /**
     * 错误注入测试-抛异常
     *
     * @return 抛异常-由agent实现
     */
    @RequestMapping("faultThrow")
    String faultThrow();

    /**
     * 错误注入测试-请求延迟
     *
     * @return 请求延迟-由agent实现
     */
    @RequestMapping("faultDelay")
    String faultDelay();

    /**
     * 仅测试联通性
     *
     * @return ok
     */
    @RequestMapping("/ping")
    String ping();

    /**
     * 测试SpringCloud注册调用
     *
     * @return 返回端口信息
     */
    @RequestMapping(value = "testCloudRegistry", method = RequestMethod.GET)
    String testCloudRegistry();

    /**
     * 获取区域
     *
     * @param exit 是否退出
     * @return 区域
     */
    @GetMapping(value = "/router/metadata")
    String getMetadata(@RequestParam("exit") boolean exit);

    /**
     * 测试优雅上下线
     *
     * @return port
     */
    @RequestMapping(value = "testGraceful", method = RequestMethod.GET)
    String testGraceful();
}
