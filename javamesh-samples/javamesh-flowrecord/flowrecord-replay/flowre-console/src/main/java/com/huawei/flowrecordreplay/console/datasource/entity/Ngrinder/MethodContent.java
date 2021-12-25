/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */
package com.huawei.flowrecordreplay.console.datasource.entity.Ngrinder;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * 接口内容
 *
 * @author lihongjiang
 * @version 0.0.1
 * @since 2021-12-10
 */
@Getter
@Setter
public class MethodContent {
    /**
     * csv文件存储之前的载体
     */
    private Map<String, String> serialized;

    /**
     * 参数化模板
     */
    private NgrinderModel ngrinderModel;

    /**
     * 获取url中问号前的内容
     */
    private String url;

    /**
     * 获取函数名称
     */
    private String method;

    /**
     * 最大参数值
     */
    private int maxParams;
}
