/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.datasource.entity.replayresult;

import lombok.Getter;
import lombok.Setter;

/**
 * 流量回放接口结果统计返回体
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-04-06
 */
@Getter
@Setter
public class ReplayInterfaceCountEntity {
    /**
     * 接口方法名称
     */
    private String method;

    /**
     * 成功的回放数目
     */
    private long successCount;

    /**
     * 失败的回放数目
     */
    private long failureCount;

    /**
     * 总的回放数目
     */
    private long total;
}
