/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.route.common.gray.label.entity;

/**
 * 路由
 *
 * @author pengyuyi
 * @date 2021/10/23
 */
public class Route {
    /**
     * 权重
     */
    private Integer weight;

    /**
     * 路由标签
     */
    private Tags tags;

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Integer getWeight() {
        return this.weight;
    }

    public void setTags(Tags tags) {
        this.tags = tags;
    }

    public Tags getTags() {
        return this.tags;
    }
}
