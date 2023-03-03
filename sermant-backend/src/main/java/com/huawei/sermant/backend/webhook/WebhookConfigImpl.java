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

package com.huawei.sermant.backend.webhook;

import java.util.UUID;

/**
 * webhook配置
 *
 * @since 2023-03-02
 * @author xuezechao
 */
public class WebhookConfigImpl implements WebHookConfig {

    /**
     * webhook id
     */
    private UUID id = UUID.randomUUID();

    /**
     * webhook 地址
     */
    private String url;

    /**
     * webhook 名称
     */
    private String name;

    /**
     * webhook 状态
     */
    private boolean enable;

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @Override
    public boolean getEnable() {
        return enable;
    }

    @Override
    public UUID getId() {
        return id;
    }
}
