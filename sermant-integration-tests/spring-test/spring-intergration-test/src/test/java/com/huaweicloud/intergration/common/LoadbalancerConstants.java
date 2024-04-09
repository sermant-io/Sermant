/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.intergration.common;

/**
 * 负载均衡常量
 *
 * @author zhouss
 * @since 2022-08-17
 */
public class LoadbalancerConstants {
    /**
     * 打包测试时指定的spring cloud版本环境变量key
     */
    public static final String SPRING_CLOUD_VERSION_ENV_KEY = "spring.cloud.version";

    /**
     * 打包测试时指定的spring boot版本环境变量key
     */
    public static final String SPRING_BOOT_VERSION_ENV_KEY = "spring.boot.version";
    private LoadbalancerConstants() {
    }
}
