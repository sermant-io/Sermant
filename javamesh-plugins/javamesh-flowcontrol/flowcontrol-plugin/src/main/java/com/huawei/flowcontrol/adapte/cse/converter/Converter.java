/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.converter;

/**
 * 转换器
 * 当前用于转换相关规则
 *
 * @param <SOURCE>  源类型
 * @param <TARGET> 目标类型
 * @author zhouss
 * @since 2021-11-16
 */
public interface Converter<SOURCE, TARGET> {
    /**
     * 转换
     *
     * @param source 源数据类型
     * @return 目前数据
     */
    TARGET convert(SOURCE source);
}
