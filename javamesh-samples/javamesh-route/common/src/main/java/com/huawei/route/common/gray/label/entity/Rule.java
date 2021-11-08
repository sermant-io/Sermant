/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.route.common.gray.label.entity;

import java.util.List;

/**
 * 规则
 *
 * @author pengyuyi
 * @date 2021/10/27
 */
public class Rule {
    /**
     * 优先级，值越小优先级越高
     */
    private int precedence;

    /**
     * 匹配规则
     */
    private Match match;

    /**
     * 路由
     */
    private List<Route> route;

    public void setPrecedence(int precedence) {
        this.precedence = precedence;
    }

    public int getPrecedence() {
        return this.precedence;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public Match getMatch() {
        return this.match;
    }

    public void setRoute(List<Route> route) {
        this.route = route;
    }

    public List<Route> getRoute() {
        return this.route;
    }
}
