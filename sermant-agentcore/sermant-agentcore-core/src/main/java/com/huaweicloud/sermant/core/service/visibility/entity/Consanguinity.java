/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.sermant.core.service.visibility.entity;

import java.util.List;

/**
 * 血缘关系信息
 *
 * @author zhp
 * @since 2022-11-30
 */
public class Consanguinity extends BaseInfo {
    /**
     * 接口名称
     */
    private String interfaceName;

    /**
     * URL路径
     */
    private String url;

    /**
     * 服务注册时的key
     */
    private String serviceKey;

    /**
     * 服务提供者信息
     */
    private List<Contract> providers;

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Contract> getProviders() {
        return providers;
    }

    public void setProviders(List<Contract> providers) {
        this.providers = providers;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }
}