/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.apm.core.service.dynamicconfig.kie.kie;

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
