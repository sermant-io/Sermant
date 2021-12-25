/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */
package com.huawei.flowrecordreplay.console.datasource.entity.Ngrinder;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 引流压测所有接口模板
 *
 * @author lihongjiang
 * @version 0.0.1
 * @since 2021-12-10
 */
@Getter
@Setter
public class NgrinderModels {
    /**
     * 参数化模板map
     */
    private Map<String, NgrinderModel> ngrinderModelMap;

    /**
     * total数目
     */
    private long total;

    /**
     * csv文件url
     */
    private String csvFileUrl;
}
