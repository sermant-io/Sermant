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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 默认请求统计
 *
 * @author zhouss
 * @since 2022-06-20
 */
public class DefaultRequestStat extends BaseRequestStat {
    @JSONField(name = "错误数")
    protected final AtomicInteger errorCount = new AtomicInteger();

    @JSONField(name = "成功数")
    protected final AtomicInteger successCount = new AtomicInteger();

    /**
     * 构造器
     *
     * @param resource 资源名称
     */
    public DefaultRequestStat(String resource) {
        super(resource);
    }

    @Override
    public void stat(Throwable throwable, ResponseInfo responseInfo) {
        if (throwable == null) {
            successCount.incrementAndGet();
        } else {
            errorCount.incrementAndGet();
        }
        super.stat(throwable, responseInfo);
    }

    @Override
    public String toString() {
        return "DefaultRequestStat{"
                + "allCount=" + allCount
                + ", resource='" + resource + '\''
                + ", errorCount=" + errorCount
                + ", successCount=" + successCount
                + '}';
    }

    public AtomicInteger getErrorCount() {
        return errorCount;
    }

    public AtomicInteger getSuccessCount() {
        return successCount;
    }
}
