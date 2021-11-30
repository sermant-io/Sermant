/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.flowreplay.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * 回放结果中的字段的数据结构
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-04-12
 */
@Getter
@Setter
public class Field {
    /**
     * 接口下的字段名
     */
    private String name;

    /**
     * 字段的忽略情况
     */
    private boolean ignore;
}
