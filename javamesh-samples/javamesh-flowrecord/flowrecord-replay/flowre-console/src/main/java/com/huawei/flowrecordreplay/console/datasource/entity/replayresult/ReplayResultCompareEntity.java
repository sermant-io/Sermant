/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.datasource.entity.replayresult;

import com.alibaba.fastjson.JSONObject;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 流量回放结果概览
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-04-07
 */
@Getter
@Setter
public class ReplayResultCompareEntity {
    /**
     * 回放结果列表
     */
    private List<JSONObject> replayResultCompareEntityList;

    /**
     * 回放总数
     */
    private long total;
}
