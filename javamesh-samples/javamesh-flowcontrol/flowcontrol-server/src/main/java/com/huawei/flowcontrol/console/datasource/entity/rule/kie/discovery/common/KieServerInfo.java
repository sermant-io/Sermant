/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.datasource.entity.rule.kie.discovery.common;

import com.huawei.flowcontrol.console.entity.AppInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * kie服务信息
 *
 * @author Sherlockhan
 * @since 2020-12-21
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class KieServerInfo extends AppInfo {
    private String id;

    private KieServerLabel label;

    @Override
    public int hashCode() {
        return label.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof KieServerInfo)) {
            return false;
        }
        KieServerInfo that = (KieServerInfo) obj;
        return label.equals(that.label);
    }

    @Override
    public String toString() {
        return new StringBuilder("KieServerInfo {")
            .append("label='").append(label).append('\'')
            .append(", id='").append(id).append('\'')
            .append('}').toString();
    }
}
