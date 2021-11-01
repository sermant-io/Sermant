/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.entity;

import com.huawei.route.server.constants.RouteConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;

/**
 * 定义标签
 *
 * @author zhouss
 * @since 2021-10-12
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Tag extends HashMap<String, String> {
    private static Tag defaultTag;

    /**
     * 获取标签  指定tagName
     *
     * @return 返回当前标签
     */
    public String getTagName() {
        return this.get(RouteConstants.TAG_NAME_KEY);
    }

    /**
     * 构建版本标签
     *
     * @param tagName 标签版本
     * @return tag
     */
    @SuppressWarnings("unused")
    public Tag putTagName(String tagName) {
        this.put(RouteConstants.TAG_NAME_KEY, tagName);
        return this;
    }

    public Tag addTag(String tagKey, String tagName) {
        this.put(tagKey, tagName);
        return this;
    }

    /**
     * 默认标签
     *
     * @return 默认标签
     */
    public static Tag getDefaultTag() {
        if (defaultTag == null) {
            defaultTag = new Tag()
                    .addTag(RouteConstants.TAG_NAME_KEY, RouteConstants.DEFAULT_TAG_NAME)
                    .addTag(RouteConstants.LDC_KEY, RouteConstants.DEFAULT_LDC);
        }
        return defaultTag;
    }
}
