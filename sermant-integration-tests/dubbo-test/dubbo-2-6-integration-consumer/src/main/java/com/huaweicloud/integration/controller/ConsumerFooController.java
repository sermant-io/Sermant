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

package com.huaweicloud.integration.controller;

import com.huaweicloud.integration.service.FooService;

import com.alibaba.dubbo.rpc.RpcContext;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;

import javax.annotation.Resource;

/**
 * 测试接口
 *
 * @author provenceee
 * @since 2022-04-28
 */
@RestController
@RequestMapping("/consumer")
public class ConsumerFooController {
    private static final String TAG = "app1";

    private static final String TAG_KEY = "dubbo.tag";

    @Resource(name = "fooService")
    private FooService fooService;

    /**
     * 测试接口
     *
     * @param str 参数
     * @return 测试信息
     */
    @GetMapping("/testFoo")
    public String testFoo(@RequestParam String str) {
        RpcContext.getContext().setAttachment(TAG_KEY, TAG);
        return fooService.foo(str);
    }

    /**
     * 测试接口
     *
     * @param str 参数
     * @return 测试信息
     */
    @GetMapping("/testFoo2")
    public String testFoo2(@RequestParam String str) {
        RpcContext.getContext().setAttachment(TAG_KEY, TAG);
        return fooService.foo2(str);
    }

    /**
     * 测试tag接口
     *
     * @param tag tag
     * @return 测试信息
     * @throws HttpServerErrorException http异常
     */
    @GetMapping("/testTag")
    public String testTag(@RequestParam String tag) {
        // dubbo2.6.6+才支持tag功能，跑流水时暂时以代码的方式模拟报错，手动测试时可以屏蔽这段报错的逻辑
        if (!TAG.equals(tag)) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        RpcContext.getContext().setAttachment(TAG_KEY, tag);
        return fooService.foo2(tag);
    }

    /**
     * 获取注册协议
     *
     * @return 注册协议
     */
    @GetMapping("/getRegistryProtocol")
    public String getRegistryProtocol() {
        RpcContext.getContext().setAttachment(TAG_KEY, TAG);
        return fooService.getRegistryProtocol();
    }

    /**
     * 获取区域
     *
     * @param exit 是否退出
     * @return 区域
     */
    @GetMapping("/getMetadata")
    public String getMetadata(boolean exit) {
        RpcContext.getContext().setAttachment(TAG_KEY, TAG);
        return fooService.getMetadata(exit);
    }
}