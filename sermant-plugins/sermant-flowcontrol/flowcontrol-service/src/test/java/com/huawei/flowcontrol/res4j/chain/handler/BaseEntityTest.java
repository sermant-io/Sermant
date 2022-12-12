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

package com.huawei.flowcontrol.res4j.chain.handler;

import com.huawei.flowcontrol.common.entity.DubboRequestEntity;
import com.huawei.flowcontrol.common.entity.HttpRequestEntity;
import com.huawei.flowcontrol.common.entity.HttpRequestEntity.Builder;
import com.huawei.flowcontrol.common.entity.RequestEntity.RequestType;

import java.util.Collections;

/**
 * 提供请求实体
 *
 * @author zhouss
 * @since 2022-08-30
 */
public class BaseEntityTest {
    final HttpRequestEntity httpClientEntity = new Builder()
            .setApiPath(RequestTest.API_PATH)
            .setMethod("POST")
            .setRequestType(RequestType.CLIENT)
            .build();
    final HttpRequestEntity httpServerEntity = new Builder()
            .setApiPath(RequestTest.API_PATH)
            .setMethod("POST")
            .setRequestType(RequestType.SERVER)
            .build();
    final DubboRequestEntity dubboServerEntity = new DubboRequestEntity(RequestTest.API_PATH, Collections.emptyMap(),
            RequestType.SERVER, "application");

    final DubboRequestEntity dubboClientEntity = new DubboRequestEntity(RequestTest.API_PATH, Collections.emptyMap(),
            RequestType.CLIENT, "application");
}
