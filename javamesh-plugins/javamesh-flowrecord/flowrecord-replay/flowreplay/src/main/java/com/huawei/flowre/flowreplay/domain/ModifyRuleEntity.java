/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.flowreplay.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * 回放数据入参修改规则
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-04-19
 */
@Getter
@Setter
public class ModifyRuleEntity {
    /**
     * 流量修改类型，仅支持“Concrete”和“Regex”
     */
    String type;

    /**
     * 流量修改的具体查找值或正则表达式
     */
    String search;

    /**
     * 流量修改的替换值
     */
    String replacement;
}
