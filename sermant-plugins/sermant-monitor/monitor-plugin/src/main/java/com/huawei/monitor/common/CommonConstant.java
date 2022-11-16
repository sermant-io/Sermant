/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.monitor.common;

/**
 * 普通常量类
 *
 * @author zhp
 * @since 2022-11-02
 */
public class CommonConstant {
    /**
     * dubbo客户端
     */
    public static final String DUBBO_CONSUMER = "consumer";

    /**
     * dubbo服务端
     */
    public static final String DUBBO_PROVIDER = "provider";

    /**
     * 区分dubbo调用端 provider 服务端 consumer 客户端
     */
    public static final String DUBBO_SIDE = "side";

    /**
     * dubbo从url中获取当前服务名
     *
     * @see org.apache.dubbo.common.URL#getParameter(String)
     * @see com.alibaba.dubbo.common.URL#getParameter(String)
     */
    public static final String DUBBO_APPLICATION = "application";

    private CommonConstant() {
    }
}
