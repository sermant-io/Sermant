/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.implement.service.dynamicconfig.nacos;

/**
 * Nacos连接异常
 *
 * @author tangle
 * @since 2023-08-17
 */
public class NacosInitException extends RuntimeException {
    private static final long serialVersionUID = -5916948812185593365L;

    /**
     * nacos连接失败
     *
     * @param connectString 连接字符串
     */
    public NacosInitException(String connectString) {
        super("Connect to " + connectString + " failed. ");
    }
}
