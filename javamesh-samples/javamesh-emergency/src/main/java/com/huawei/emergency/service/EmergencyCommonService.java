/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.service;

import com.huawei.common.api.CommonResult;

/**
 * 通过增删改接口
 *
 * @param <T>
 * @author y30010171
 * @since 2021-11-04
 **/
public interface EmergencyCommonService<T> {
    /**
     * 新增
     *
     * @param t 新增的记录
     * @return {@link CommonResult} 通过{@link CommonResult#getData()} 获取新增的记录
     */
    CommonResult<T> add(T t);

    /**
     * 删除
     *
     * @param t 需要删除的记录
     * @return {@link CommonResult}
     */
    CommonResult delete(T t);

    /**
     * 修改
     *
     * @param t 需要修改的记录
     * @return {@link CommonResult}
     */
    CommonResult update(T t);
}
