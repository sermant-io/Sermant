/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.rules.define;

import com.huawei.route.server.rules.define.matcher.Matcher;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 单个目标服务规则
 *
 * @author zhouss
 * @since 2021-10-23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TargetServiceRule {
    /**
     * 优先级
     * 越小优先级越高
     */
    private Integer precedence;

    /**
     * 匹配器
     */
    private Matcher match;

    /**
     * 路由规则
     */
    private List<WeightRule> route;

    /**
     * 当前规则是否合法
     *
     * @return boolean
     */
    public boolean isValid() {
        if (CollectionUtils.isEmpty(route)) {
            return false;
        }
        for (WeightRule rule : route) {
            if (!rule.isValid()) {
                return false;
            }
        }
        return precedence != null && precedence >= 0 && match != null;
    }
}
