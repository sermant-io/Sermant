/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.recordconsole.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * zookeeper中groovy脚本信息
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-04-13
 */
@Getter
@Setter
public class GroovyInfoEntity {
    /**
     * 脚本资源URL
     */
    private String url;

    /**
     * 脚本文件名（带后缀）
     */
    private String scriptName;

    /**
     * 脚本脱敏流程函数名
     */
    private String functionName;
}
