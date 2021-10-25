/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.apm.core.query;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 记录列表封装，添加总记录数
 *
 * @author zhouss
 * @since 2020-12-09
 **/
@Getter
@Setter
public class NodeRecords {
    private List<NodeRecord> records;

    /**
     * 总记录数
     */
    private long total;

    public NodeRecords() {
        records = new ArrayList<>();
    }
}
