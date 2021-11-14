/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.rules.define.matcher;

import lombok.Data;

/**
 * 用户自定义match
 *
 * @author zhouss
 * @since 2021-10-23
 */
@Data
public class ReferMatcher implements Matcher {
    /**
     * 自定义refer名称
     */
    private String refer;
}
