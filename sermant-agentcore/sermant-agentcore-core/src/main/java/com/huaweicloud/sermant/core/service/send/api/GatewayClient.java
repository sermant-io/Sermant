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

package com.huaweicloud.sermant.core.service.send.api;

import com.huaweicloud.sermant.core.service.BaseService;

/**
 * 网关的客户端
 *
 * @since 2022-03-26
 */
public interface GatewayClient extends BaseService {

    /**
     * 向统一网关发送数据
     *
     * @param data 数据字节
     * @param typeNum 数据类型号
     */
    void send(byte[] data, int typeNum);

    /**
     * 将传输对象序列化，并向统一网关发送数据
     *
     * @param object 待传输对象
     * @param typeNum 消息类型
     */
    void send(Object object, int typeNum);

    /**
     * [立刻发送]向统一网关发送数据
     *
     * @param data 数据字节
     * @param typeNum 数据类型号
     * @return boolean 是否发送成功
     */
    boolean sendImmediately(byte[] data, int typeNum);

    /**
     * [立刻发送]将传输对象序列化，并向统一网关发送数据
     *
     * @param object 待传输对象
     * @param typeNum 消息类型
     * @return boolean 是否发送成功
     */
    boolean sendImmediately(Object object, int typeNum);
}
