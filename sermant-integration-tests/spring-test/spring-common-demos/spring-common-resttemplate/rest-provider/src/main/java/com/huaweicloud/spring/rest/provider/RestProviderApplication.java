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

package com.huaweicloud.spring.rest.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类
 *
 * @author zhouss
 * @since 2022-07-28
 */
@SpringBootApplication(scanBasePackages = {
    "com.huaweicloud.spring.common.flowcontrol.provider",
    "com.huaweicloud.spring.rest.provider",
    "com.huaweicloud.spring.common.loadbalancer.provider",
    "com.huaweicloud.spring.common.registry.provider"
})
public class RestProviderApplication {
    /**
     * 启动
     *
     * @param args 参数
     */
    public static void main(String[] args) {
        SpringApplication.run(RestProviderApplication.class, args);
    }
}
