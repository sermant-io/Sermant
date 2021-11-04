/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.controller;

import com.huawei.route.common.Result;
import com.huawei.route.server.labels.label.service.LabelService;
import com.huawei.route.server.labels.vo.LabelBusinessVo;
import com.huawei.route.server.labels.vo.LabelValidVo;
import com.huawei.route.server.labels.vo.LabelVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 标签与前端交互的控制类
 *
 * @author Zhang Hu
 * @since 2021-04-14
 */
@RestController
public class LabelController {
    private final LabelService labelService;

    @Autowired
    public LabelController(LabelService labelService) {
        this.labelService = labelService;
    }

    /**
     * 添加标签
     *
     * @param label 标签信息
     * @return Result
     */
    @PostMapping("/label/add")
    public Result<String> addLabel(@Validated @RequestBody LabelVo label) {
        return labelService.addLabel(label);
    }

    /**
     * 更新标签
     *
     * @param label 标签信息
     * @return Result
     */
    @PostMapping("/label/update")
    public Result<String> updateLabel(@Validated @RequestBody LabelVo label) {
        return labelService.updateLabel(label);
    }

    /**
     * 删除标签
     *
     * @param labelGroupName 标签组名称
     * @param labelName      标签名
     * @return Result
     */
    @PostMapping("/label/delete")
    public Result<String> deleteLabel(String labelGroupName, String labelName) {
        return labelService.deleteLabel(labelGroupName, labelName);
    }

    /**
     * 获取标签组下的标签
     *
     * @param labelGroupName 标签组名称
     * @return Result
     */
    @GetMapping("/labels/labelGroupName")
    public Result<Object> getLabels(String labelGroupName) {
        return labelService.selectLabels(labelGroupName);
    }

    /**
     * 标签生效
     *
     * @param labelValidVo 标签信息
     * @return Result
     */
    @PostMapping("/label/isValid")
    public Result<Object> labelValidAndInvalid(@Validated @RequestBody LabelValidVo labelValidVo) {
        return labelService.labelValidAndInvalid(labelValidVo);
    }

    /**
     * 标签编辑生效
     *
     * @param labelBusinessVo 标签信息
     * @return Result
     */
    @PostMapping("/label/business")
    public Result<String> labelEditBusiness(@Validated @RequestBody LabelBusinessVo labelBusinessVo) {
        return labelService.editLabelInstance(labelBusinessVo);
    }

    /**
     * 查询该标签的实例列表
     *
     * @param labelGroupName 标签组
     * @param labelName 标签名
     * @return Result
     */
    @GetMapping("/label/instance")
    public Result<List<Object>> selectLabelInstance(String labelGroupName, String labelName) {
        return labelService.selectLabelInstance(labelGroupName, labelName);
    }
}
