/*
 *  Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.huaweicloud.demo.tagtransmission.rpc.api.apachedubbo;

/**
 * apache dubbo 的接口服务
 *
 * @author daizhenyu
 * @since 2023-09-07
 **/
public interface ApacheTagTransmissionService {
    /**
     * 接口服务方法
     *
     * @return 流量标签
     */
    String transmitTag();
}
