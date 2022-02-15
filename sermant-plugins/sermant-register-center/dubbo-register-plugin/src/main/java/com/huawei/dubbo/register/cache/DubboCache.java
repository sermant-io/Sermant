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

package com.huawei.dubbo.register.cache;

/**
 * dubbo应用名缓存
 *
 * @author provenceee
 * @since 2021/12/23
 */
public enum DubboCache {
    /**
     * 单例缓存
     */
    INSTANCE;

    /**
     * 服务名
     */
    private String serviceName;

    /**
     * 是否加载了sc的注册spi的标志
     */
    private boolean isLoadSc;

    /**
     * 加载的url的class(alibaba/apache)
     */
    private Class<?> urlClass;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * 加载sc spi
     */
    public void loadSc() {
        isLoadSc = true;
    }

    public boolean isLoadSc() {
        return isLoadSc;
    }

    public Class<?> getUrlClass() {
        return urlClass;
    }

    public void setUrlClass(Class<?> urlClass) {
        this.urlClass = urlClass;
    }
}