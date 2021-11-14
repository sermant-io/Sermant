/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.controller.monitor;

import java.util.Map;

/**
 * 功能描述：查询结果对象封装
 *
 * @author z30009938
 * @since 2021-11-12
 */
public class MonitorModel {
    /**
     * 查询结果是否成功
     */
    private boolean success;

    /**
     * 查询数据
     */
    private Map<String, Object> data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
