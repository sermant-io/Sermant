/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.dubbo.register;

/**
 * 发现下游服务新增实例的实体
 *
 * @author provenceee
 * @since 2021/12/15
 */
public class Subscription {
    private final Object url;
    private final Object notifyListener;

    /**
     * 构造方法
     *
     * @param url url
     * @param notifyListener notifyListener
     */
    public Subscription(Object url, Object notifyListener) {
        this.url = url;
        this.notifyListener = notifyListener;
    }

    public Object getUrl() {
        return url;
    }

    public Object getNotifyListener() {
        return notifyListener;
    }
}