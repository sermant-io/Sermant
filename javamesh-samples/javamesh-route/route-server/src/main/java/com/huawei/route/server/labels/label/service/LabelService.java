/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.label.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huawei.route.server.common.Result;
import com.huawei.route.server.labels.vo.LabelBusinessVo;
import com.huawei.route.server.labels.vo.LabelValidVo;
import com.huawei.route.server.labels.vo.LabelVo;
import com.huawei.route.server.labels.vo.ScheduleLabelValidVo;

import java.util.List;

/**
 * 标签服务接口
 *
 * @author Zhang Hu
 * @since 2021-04-12
 */
public interface LabelService {
    /**
     * 添加标签
     *
     * @param label 标签对象
     * @return 结果字符串
     */
    Result<String> addLabel(LabelVo label);

    /**
     * 修改标签信息
     *
     * @param label 标签信息对象
     * @return 结果字符串
     */
    Result<String> updateLabel(LabelVo label);

    /**
     * 删除标签信息
     *
     * @param labelGroupName 标签组名
     * @param labelName 标签名
     * @return 结果信息
     */
    Result<String> deleteLabel(String labelGroupName, String labelName);

    /**
     * 查看标签信息
     *
     * @param labelGroupName 标签组名，如果不传入标签组名，则查询所有标签组的标签信息
     * @return 标签信息集合
     */
    Result<Object> selectLabels(String labelGroupName);

    /**
     * 编辑标签受用业务
     *
     * @param labelBusiness 标签信息，
     * @return 结果字符串
     */
    Result<String> editLabelInstance(LabelBusinessVo labelBusiness);

    /**
     * 标签生效与失效
     *
     * @param labelValid 标签信息对象，从redis获取信息并发送到netty服务端
     * @return 字符串
     */
    Result<Object> labelValidAndInvalid(LabelValidVo labelValid);

    /**
     * 实例启动时生效
     *
     * @param labelValidList 定时生效的信息对象
     * @return 结果
     */
    boolean instanceStartValid(List<ScheduleLabelValidVo> labelValidList);

    /**
     * 查询标签所有业务
     *
     * @param labelGroupName 标签组名
     * @param labelName 标签名
     * @return 标签业务集合
     */
    Result<List<Object>> selectLabelInstance(String labelGroupName, String labelName);

    /**
     * 查询标签所有业务  返回JSONArray， 转换之前的数据
     *
     * @param labelGroupName 标签组名
     * @param labelName 标签名
     * @return 标签业务集合
     */
    JSONArray selectRawLabelInstance(String labelGroupName, String labelName);

    /**
     * 查询标签所有业务  返回JSONArray， 转换之前的数据, 只查询打个服务的所有实例
     *
     * @param serviceName 标签库服务名
     * @param labelGroupName 标签组名
     * @param labelName 标签名
     * @param label 标签数据 ，当为空的时，主动查询标签数据
     * @return 标签业务集合
     */
    JSONObject selectRawLabelInstance(String serviceName, String labelGroupName, String labelName, JSONObject label);

    /**
     *
     * 查询单个实例的标签配置
     *
     * @param serviceName 标签库服务名
     * @param labelGroupName 标签组名
     * @param labelName 标签名
     * @param instanceName 标签实例名
     * @return 标签业务集合
     */
    JSONObject selectSingleInstance(String serviceName, String instanceName, String labelGroupName, String labelName);

    /**
     * 服务实例启动后保存服务标签的数据
     *
     * @param serviceName 服务名
     * @return 标签
     */
    List<JSONObject> instanceStartLabelValid(String serviceName);

    /**
     * 删除生效的标签信息
     *
     * @param serviceName 服务名
     * @param instanceName 实例名
     */
    void deleteTempLabel(String serviceName, String instanceName);
}
