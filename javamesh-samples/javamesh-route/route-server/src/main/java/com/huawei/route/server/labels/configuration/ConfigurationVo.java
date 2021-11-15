/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.configuration;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

/**
 * 返回给前端的查询结果类
 *
 * @author Zhang Hu
 * @since 2021-04-15
 */
@Data
public class ConfigurationVo implements Serializable {
    private static final long serialVersionUID = 4719163406667672352L;

    private String configName;

    private String configValue;

    private String description;

    private JSONObject envs;

    /**
     * 修改时间时间戳
     */
    @JsonIgnore
    private Long updateTimeStamp;
}
