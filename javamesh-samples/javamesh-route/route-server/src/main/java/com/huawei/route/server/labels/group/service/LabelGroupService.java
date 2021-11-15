/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.group.service;

import com.huawei.route.common.Result;
import com.huawei.route.server.labels.group.LabelGroup;

/**
 * 标签组操作接口，提供操作标签组的方法，包含增删改查
 *
 * @author Zhang Hu
 * @since 2021-04-09
 */
public interface LabelGroupService {
    /**
     * 添加标签组
     *
     * @param labelGroup 标签组对象
     * @return LabelGroup 添加成功返回true，否则为false
     */
    Result<LabelGroup> addLabelGroup(LabelGroup labelGroup);

    /**
     * 修改标签组
     *
     * @param labelGroup 标签组对象
     * @return LabelGroup 修改成功返回true，否则为false
     */
    Result<LabelGroup> updateLabelGroup(LabelGroup labelGroup);

    /**
     * 删除标签组
     *
     * @param labelGroupName 标签组名
     * @return String 删除成功返回true，否则为false
     */
    Result<String> deleteLabelGroup(String labelGroupName);

    /**
     * 查询所有标签组信息
     *
     * @return result 标签组信息的集合
     */
    Result<Object> getLabelGroups();

    /**
     * 检查标签组是否存在
     *
     * @param labelGroupName 标签组名
     * @return 标签组是否存在
     */
    boolean checkLabelGroupIsExist(String labelGroupName);
}
