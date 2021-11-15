/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.rules.define;

import com.huawei.route.server.entity.Tag;
import lombok.Data;

/**
 * 规则定义
 *
 * @author zhouss
 * @since 2021-10-23
 */
@Data
public abstract class AbstractRule {
    private Tag tags;
}
