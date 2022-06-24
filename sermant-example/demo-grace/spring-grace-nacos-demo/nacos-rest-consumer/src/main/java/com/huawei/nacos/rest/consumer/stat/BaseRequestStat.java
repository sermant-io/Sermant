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

package com.huawei.nacos.rest.consumer.stat;

import com.huawei.nacos.rest.consumer.entity.ResponseInfo;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 请求统计
 *
 * @author zhouss
 * @since 2022-06-17
 */
public class BaseRequestStat implements Serializable, RequestStat {
    @JSONField(name = "总请求数")
    protected final AtomicInteger allCount = new AtomicInteger();

    @JSONField(name = "请求接口")
    protected final String resource;

    /**
     * 构造器
     *
     * @param resource 资源名称
     */
    public BaseRequestStat(String resource) {
        this.resource = resource;
    }

    @Override
    public void stat(Throwable throwable, ResponseInfo responseInfo) {
        allCount.incrementAndGet();
    }

    @Override
    public String toString() {
        return "BaseRequestStat{"
                + ", allCount=" + allCount.get()
                + ", resource='" + resource + '\''
                + '}';
    }

    public AtomicInteger getAllCount() {
        return allCount;
    }

    public String getResource() {
        return resource;
    }
}
