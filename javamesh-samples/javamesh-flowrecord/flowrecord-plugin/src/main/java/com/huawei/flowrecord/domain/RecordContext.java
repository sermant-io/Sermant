/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowrecord.domain;

/**
 * record's context for transfer information from before method to after method
 *
 */

public class RecordContext {
    public String requestBody;

    public String requestClass;

    public boolean isConsumer;
}
