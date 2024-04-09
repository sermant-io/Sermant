/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

import com.huaweicloud.integration.service.BarService;

import com.alibaba.dubbo.rpc.service.GenericService;

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
@RequestMapping("/consumer")
public class ConsumerBarController {
    @Resource(name = "barService")
    private BarService barService;

    @Resource(name = "bar2Service")
    private BarService bar2Service;

    @Resource(name = "barGenericService")
    private GenericService barGenericService;

    /**
     * 测试接口
     *
     * @param str 参数
     * @return 测试信息
     */
    @GetMapping("/testBar")
    public String testBar(@RequestParam String str) {
        return barService.bar(str);
    }

    /**
     * 测试接口
     *
     * @param str 参数
     * @return 测试信息
     */
    @GetMapping("/testBar2")
    public String testBar2(@RequestParam String str) {
        return bar2Service.bar(str);
    }

    /**
     * 测试泛化接口
     *
     * @param str 参数
     * @return 测试信息
     */
    @GetMapping("/testBarGeneric")
    public String testBarGeneric(@RequestParam String str) {
        return barGenericService.$invoke("bar", new String[]{"java.lang.String"}, new Object[]{str})
            .toString();
    }
}