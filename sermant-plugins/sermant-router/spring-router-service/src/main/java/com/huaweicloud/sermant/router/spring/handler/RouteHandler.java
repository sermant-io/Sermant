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

package com.huaweicloud.sermant.router.spring.handler;

import com.huaweicloud.sermant.router.common.request.RequestData;

import java.util.List;

/**
 * 路由处理器接口
 *
 * @author lilai
 * @since 2023-02-21
 */
public interface RouteHandler {

    /**
     * 调用路由处理器链
     *
     * @param targetName 目标服务名
     * @param instances 被筛选的服务实力列表
     * @param requestData 请求数据
     * @return 筛选后的实例列表
     */
    List<Object> handle(String targetName, List<Object> instances, RequestData requestData);

    /**
     * 处理器的优先级
     *
     * @return 优先级序号
     */
    int getOrder();
}
