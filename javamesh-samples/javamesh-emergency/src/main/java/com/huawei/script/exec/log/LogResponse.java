/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.script.exec.log;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 数据结构，用于描述脚本执行时的实时日志
 *
 * @author y30010171
 * @since 2021-10-25
 **/
@Data
@AllArgsConstructor
public class LogResponse {
    /**
     * 当前日志的行号。需要为null，代表后续没有日志产生了
     */
    private Integer line;
    private String[] data;
}
