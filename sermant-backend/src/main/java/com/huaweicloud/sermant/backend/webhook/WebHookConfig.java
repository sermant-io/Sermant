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
 * WebHook config interface
 *
 * @author xuezechao
 * @since 2023-03-02
 */
public interface WebHookConfig {

    /**
     * Set url
     *
     * @param url webhook url
     */
    void setUrl(String url);

    /**
     * Get the webhook url
     *
     * @return webhook url
     */
    String getUrl();

    /**
     * Set the webhook name
     *
     * @param name webhook name
     */
    void setName(String name);

    /**
     * Get the webhook name
     *
     * @return webhook name
     */
    String getName();

    /**
     * Set the webhook status
     *
     * @param enable webhook status
     */
    void setEnable(boolean enable);

    /**
     * Get the webhook status
     *
     * @return status
     */
    boolean getEnable();

    /**
     * Get the webhook id
     *
     * @return webhook id
     */
    int getId();

    /**
     * Set the webhook id
     *
     * @param id webhook id
     */
    void setId(int id);
}
