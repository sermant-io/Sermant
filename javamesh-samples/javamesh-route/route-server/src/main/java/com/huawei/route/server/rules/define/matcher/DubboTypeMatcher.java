/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.rules.define.matcher;

import com.huawei.route.server.rules.define.key.KeyPair;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Dubbo类型匹配
 *
 * @author zhouss
 * @since 2021-10-23
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DubboTypeMatcher extends TypeMatcher{
    /**
     * 参数匹配
     */
    private KeyPair<String, Object> args;
}
