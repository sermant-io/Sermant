/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.core.service.send;

import com.huawei.sermant.core.service.BaseService;

/**
 * 网关的客户端
 */
public interface GatewayClient extends BaseService {

    /**
     * 向统一网关发送数据
     * @param data 数据字节
     * @param typeNum 数据类型号
     */
    void send(byte[] data, int typeNum);
}
