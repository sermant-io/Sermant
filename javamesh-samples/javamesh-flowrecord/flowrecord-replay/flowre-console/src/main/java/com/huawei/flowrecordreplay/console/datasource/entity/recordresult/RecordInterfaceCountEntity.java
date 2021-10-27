/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.datasource.entity.recordresult;

import lombok.Getter;
import lombok.Setter;

/**
 * 录制流量接口名称及总数
 *
 * @author lihongjiang
 * @version 0.0.1
 * @since 2021-06-10
 */
@Getter
@Setter
public class RecordInterfaceCountEntity {
    /**
     * 接口方法名称
     */
    private String method;

    /**
     * 接口方法数目
     */
    private long total;
}
