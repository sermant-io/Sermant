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

package com.huaweicloud.sermant.backend.webhook;

/**
 * webhook配置接口
 *
 * @author xuezechao
 * @since 2023-03-02
 */
public interface WebHookConfig {

    /**
     * 设置url
     *
     * @param url webhook 地址
     */
    void setUrl(String url);

    /**
     * 获取webhook 地址
     *
     * @return 地址
     */
    String getUrl();

    /**
     * 设置webhook 名称
     *
     * @param name 名称
     */
    void setName(String name);

    /**
     * 获取webhook 名称
     *
     * @return 名称
     */
    String getName();

    /**
     * 设置webhook状态
     *
     * @param enable 状态
     */
    void setEnable(boolean enable);

    /**
     * 获取webhook 状态
     *
     * @return 状态
     */
    boolean getEnable();

    /**
     * 获取webhook id
     *
     * @return id
     */
    int getId();

    /**
     * 设置webhook id
     *
     * @param id id
     */
    void setId(int id);
}
