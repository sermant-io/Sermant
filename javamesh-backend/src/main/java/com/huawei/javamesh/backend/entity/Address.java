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

package com.huawei.javamesh.backend.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Address {
    /**
     * 主机名，可以是ip也可以是域名
     */
    private String host;

    /**
     * 端口
     */
    private int port;

    /**
     * 安全端口
     */
    private int sport;

    /**
     * inner或者outer，代表是内网还是外网，地址优先链接内网的
     */
    private AddressType type;

    /**
     * 内外
     */
    private AddressScope scope;

    /*
     * 协议,当前只支持ws
     */
    private Protocol protocol;
}
