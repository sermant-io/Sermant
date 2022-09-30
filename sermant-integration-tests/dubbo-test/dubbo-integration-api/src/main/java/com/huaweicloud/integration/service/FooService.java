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

package com.huaweicloud.integration.service;

/**
 * 测试接口
 *
 * @author provenceee
 * @since 2022-04-27
 */
public interface FooService {
    /**
     * 测试接口
     *
     * @param str 参数
     * @return 测试信息
     */
    String foo(String str);

    /**
     * 测试接口
     *
     * @param str 参数
     * @return 测试信息
     */
    String foo2(String str);

    /**
     * 获取注册协议
     *
     * @return 注册协议
     */
    String getRegistryProtocol();

    /**
     * 获取区域
     *
     * @param exit 是否退出
     * @return 区域
     */
    String getZone(boolean exit);
}