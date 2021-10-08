/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowreplay.mockclient.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * 从数据库获得的原始response中的关键信息(返回值类型名，返回值序列化字符串等)
 *
 * @author luanwenfei
 * @version 0.0.1 2021-02-22
 * @since 2021-02-22
 */
@Getter
@Setter
public class SelectResult {
    /**
     * 子调用返回结果 序列化后的字符串
     */
    private String selectContent;

    /**
     * 子调用返回结果的类型
     */
    private String selectClassName;
}
