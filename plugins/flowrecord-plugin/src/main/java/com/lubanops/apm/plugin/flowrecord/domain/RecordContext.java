/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.domain;

/**
 * record's context for transfer information from before method to after method
 *
 * @author lihongjiang
 * @version 0.1
 * @since 2021-02-19
 */

public class RecordContext {
    public String requestBody;

    public String requestClass;

    public boolean isConsumer;
}
