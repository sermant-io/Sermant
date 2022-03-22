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

package com.huawei.dubbotest.impl;

import com.huawei.dubbotest.service.TestInterface;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试接口
 *
 * @since 2022-03-16
 */
@Component
public class TestInterfaceImpl implements TestInterface {
    @Value("${dubbo.registry.address}")
    private String address;

    /**
     * 测试接口
     *
     * @return 测试信息
     */
    @Override
    public Map<String, String> test() {
        Map<String, String> map = new HashMap<>();
        map.put("provider-dubbo-registry-address", address);
        return map;
    }
}