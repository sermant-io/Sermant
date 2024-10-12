/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sermant.integration.controller;

import io.sermant.integration.constants.Constant;
import io.sermant.integration.service.FooService;

import org.apache.dubbo.rpc.RpcContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 测试接口
 *
 * @author provenceee
 * @since 2022-04-28
 */
@RestController
@RequestMapping("/consumer/wildcard")
public class ConsumerFooWildcardController {
    @Resource(name = "fooWildcardService")
    private FooService fooWildcardService;

    /**
     * 测试接口
     *
     * @param str 参数
     * @return 测试信息
     */
    @GetMapping("/testFoo")
    public String testFoo(@RequestParam String str) {
        RpcContext.getContext().setAttachment(Constant.TAG_KEY, Constant.TAG);
        return fooWildcardService.foo(str);
    }

    /**
     * 测试接口
     *
     * @param str 参数
     * @return 测试信息
     */
    @GetMapping("/testFoo2")
    public String testFoo2(@RequestParam String str) {
        RpcContext.getContext().setAttachment(Constant.TAG_KEY, Constant.TAG);
        return fooWildcardService.foo2(str);
    }
}
