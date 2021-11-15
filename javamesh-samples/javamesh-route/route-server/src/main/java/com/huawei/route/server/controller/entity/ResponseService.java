/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.controller.entity;

import com.huawei.route.server.entity.AbstractInstance;
import com.huawei.route.server.entity.AbstractService;
import com.huawei.route.server.register.RegisterCenterTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 响应数据
 * 尽量避免过多的数据传递，代替{@link AbstractService} 返回
 *
 * @author zhouss
 * @since 2021-10-15
 */
@Getter
@Setter
public class ResponseService<T extends AbstractInstance> {
    private String serviceName;

    private List<T> instances;

    private RegisterCenterTypeEnum registerType;
}
