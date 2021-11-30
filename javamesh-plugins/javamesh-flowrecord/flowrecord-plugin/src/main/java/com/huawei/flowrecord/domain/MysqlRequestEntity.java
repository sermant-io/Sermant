/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowrecord.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * mysql request entity
 *
 */
@Getter
@Setter
public class MysqlRequestEntity {
    private String sql;

    private Object[] parameters;
}
