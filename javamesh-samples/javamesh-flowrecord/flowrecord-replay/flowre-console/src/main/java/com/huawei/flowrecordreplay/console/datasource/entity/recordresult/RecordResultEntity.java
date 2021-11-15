/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.datasource.entity.recordresult;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 录制流量展示数据
 *
 * @author lihongjiang
 * @version 0.0.1
 * @since 2021-06-10
 */
@Getter
@Setter
public class RecordResultEntity {
    private List<RecordEntity> recordEntities;

    private long total;
}
