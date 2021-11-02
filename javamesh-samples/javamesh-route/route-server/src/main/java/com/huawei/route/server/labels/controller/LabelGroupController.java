/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.controller;

import com.huawei.route.common.Result;
import com.huawei.route.server.labels.group.LabelGroup;
import com.huawei.route.server.labels.group.service.LabelGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 标签组控制类，提供前端对标签组操作的接口
 *
 * @author Zhang Hu
 * @since 2021-04-09
 */
@RestController
public class LabelGroupController {
    private final LabelGroupService labelGroupService;

    @Autowired
    public LabelGroupController(LabelGroupService labelGroupService) {
        this.labelGroupService = labelGroupService;
    }

    @PostMapping("/label/group/add")
    public Result<LabelGroup> addGroup(@Validated @RequestBody LabelGroup labelGroup) {
        return labelGroupService.addLabelGroup(labelGroup);
    }

    @PostMapping("/label/group/delete")
    public Result<String> deleteGroup(String labelGroupName) {
        return labelGroupService.deleteLabelGroup(labelGroupName);
    }

    @PostMapping("/label/group/update")
    public Result<LabelGroup> updateGroup(@Validated @RequestBody LabelGroup labelGroup) {
        return labelGroupService.updateLabelGroup(labelGroup);
    }

    @GetMapping("/label/groups")
    public Result getGroups() {
        return labelGroupService.getLabelGroups();
    }
}
