/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.flowreplay.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * 字段比对结果的数据结构
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-04-12
 */
@Getter
@Setter
public class FieldCompare {
    /**
     * 字段名
     */
    private String name;

    /**
     * 录制得到的字段
     */
    private String record;

    /**
     * 回放得到的字段
     */
    private String replay;

    /**
     * 字段的忽略情况
     */
    private boolean ignore;

    /**
     * 字段的比对结果
     */
    private boolean compare;
}
