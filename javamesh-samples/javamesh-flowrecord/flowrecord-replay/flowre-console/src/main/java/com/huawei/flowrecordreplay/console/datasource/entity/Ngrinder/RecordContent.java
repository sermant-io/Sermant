/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */
package com.huawei.flowrecordreplay.console.datasource.entity.Ngrinder;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * 回放任务所有录制数据
 *
 * @author lihongjiang
 * @version 0.0.1
 * @since 2021-12-10
 */
@Getter
@Setter
public class RecordContent {
    /**
     * 其中string为接口名称，list中装着一条条的录制数据
     */
    private Map<String, List<MethodContent>> recordContent;

    /**
     * 其中string为接口名称，list中装入所有的key
     */
    private Map<String, List<String>> keyList;

    /**
     * 其中string为接口名称，paramscount为参数计数
     */
    private Map<String, Integer> paramsCount;

//    private Map<String, String> urls;

    /**
     * 其中string为接口名称，methodcontent包含最大参数化内容
     */
    private Map<String, MethodContent> originModel;

    /**
     * 总数目
     */
    private long total;
}