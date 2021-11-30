/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.datasource.entity.rule.kie.discovery.common;

import lombok.Builder;
import lombok.Data;

import java.util.Objects;

/**
 * kie服务标签
 *
 * @author Sherlockhan
 * @since 2020-12-21
 */
@Data
@Builder
public class KieServerLabel {
    private String service;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof KieServerLabel)) {
            return false;
        }
        KieServerLabel that = (KieServerLabel) obj;
        return Objects.equals(service, that.service);
    }

    @Override
    public int hashCode() {
        return Objects.hash(service);
    }
}
