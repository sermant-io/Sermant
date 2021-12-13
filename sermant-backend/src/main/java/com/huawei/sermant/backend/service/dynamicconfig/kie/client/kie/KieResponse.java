/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.backend.service.dynamicconfig.kie.client.kie;

import java.util.List;

/**
 * 响应结果
 *
 * @author zhouss
 * @since 2021-11-17
 */
public class KieResponse {
    /**
     * 配置总数
     */
    private Integer total;

    /**
     * kv数据
     */
    private List<KieConfigEntity> data;

    /**
     * 响应版本
     */
    private String revision;

    /**
     * 是否改变
     */
    private boolean changed = true;

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<KieConfigEntity> getData() {
        return data;
    }

    public void setData(List<KieConfigEntity> data) {
        this.data = data;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }
}
