/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.rules.define;

import com.huawei.route.server.constants.RouteConstants;
import com.huawei.route.server.entity.Tag;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

/**
 * 权重规则
 *
 * @author zhouss
 * @since 2021-10-23
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WeightRule extends AbstractRule {
    /**
     * 权重 0-100
     */
    private Integer weight;

    /**
     * 是否合法
     *
     * @return boolean
     */
    public boolean isValid() {
        final Tag tags = getTags();
        if (tags == null) {
            return false;
        }
        if (StringUtils.isEmpty(tags.getTagName())) {
            return false;
        }
        if (weight != null) {
            return weight >= 0 && weight <= RouteConstants.MAX_WEIGHT_PERCENT;
        }
        return true;
    }
}
