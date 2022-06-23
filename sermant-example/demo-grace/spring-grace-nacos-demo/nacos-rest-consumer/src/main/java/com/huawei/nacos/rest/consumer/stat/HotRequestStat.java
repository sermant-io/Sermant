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

import com.huawei.nacos.rest.consumer.entity.MyHashMap;
import com.huawei.nacos.rest.consumer.entity.ResponseInfo;

import com.alibaba.fastjson.annotation.JSONField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 服务预热统计
 *
 * @author zhouss
 * @since 2022-06-20
 */
public class HotRequestStat extends BaseRequestStat {
    private static final Logger LOGGER = LoggerFactory.getLogger(HotRequestStat.class);

    /**
     * 请求统计缓存 key : endpoint value: 请求次数
     */
    private final Map<String, ServiceStatic> request = new MyHashMap<>();

    /**
     * 构造器
     *
     * @param resource 资源名称
     */
    public HotRequestStat(String resource) {
        super(resource);
    }

    @Override
    public void stat(Throwable throwable, ResponseInfo responseInfo) {
        if (responseInfo == null) {
            LOGGER.warn("Can not acquire response info");
            return;
        }
        super.stat(throwable, responseInfo);
        String endpoint = String.format(Locale.ENGLISH, "%s:%s", responseInfo.getIp(), responseInfo.getPort());
        final ServiceStatic serviceStatic = request
                .getOrDefault(endpoint, new ServiceStatic(endpoint, responseInfo.isOpen(),
                        responseInfo.getServiceName(), responseInfo.getQps()));
        serviceStatic.setQps(responseInfo.getQps());
        serviceStatic.requestCount.incrementAndGet();
        request.put(endpoint, serviceStatic);
    }

    @Override
    public String toString() {
        return "HotRequestStat{"
                + "所有请求数=" + allCount
                + ", 请求接口='" + resource + '\''
                + ", 请求统计=" + request
                + '}';
    }

    /**
     * 内部统计类
     *
     * @since 2022-06-11
     */
    static class ServiceStatic {
        @JSONField(name = "是否开启预热")
        private final boolean enableWarmUp;

        @JSONField(name = "服务名")
        private final String serviceName;

        @JSONField(name = "QPS")
        private int qps;

        @JSONField(name = "实例")
        private String endpoint;

        @JSONField(name = "请求数")
        private AtomicLong requestCount = new AtomicLong();

        ServiceStatic(String endpoint, boolean enableWarmUp, String serviceName, int qps) {
            this.endpoint = endpoint;
            this.enableWarmUp = enableWarmUp;
            this.serviceName = serviceName;
            this.qps = qps;
        }

        public String getServiceName() {
            return serviceName;
        }

        public boolean isEnableWarmUp() {
            return enableWarmUp;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public AtomicLong getRequestCount() {
            return requestCount;
        }

        public void setRequestCount(AtomicLong requestCount) {
            this.requestCount = requestCount;
        }

        public int getQps() {
            return qps;
        }

        public void setQps(int qps) {
            this.qps = qps;
        }

        @Override
        public String toString() {
            return "ServiceStatic{"
                    + "是否开启预热=" + enableWarmUp
                    + ", qps=" + qps
                    + ", 实例地址='" + endpoint + '\''
                    + ", 请求数=" + requestCount
                    + '}';
        }
    }

    public Map<String, ServiceStatic> getRequest() {
        return request;
    }
}
