/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.configuration.service;

import com.huawei.route.common.Result;
import com.huawei.route.server.labels.configuration.Configuration;
import com.huawei.route.server.labels.configuration.ConfigurationVo;
import com.huawei.route.server.labels.configuration.EditEnvInfo;

import java.util.List;

/**
 * 配置操作接口类
 *
 * @author Zhang Hu
 * @since 2021-04-15
 */
public interface ConfigurationService {
    /**
     * 添加配置信息
     *
     * @param configuration 配置对象
     * @return 结果字符串
     */
    Result<String> addConfiguration(Configuration configuration);

    /**
     * 修改配置信息
     *
     * @param configuration 配置对象
     * @return 结果字符串
     */
    Result<String> updateConfiguration(Configuration configuration);

    /**
     * 添加配置信息
     *
     * @param configName 配置名
     * @return 结果字符串
     */
    Result<String> deleteConfiguration(String configName);

    /**
     * 查询所有配置
     *
     * @return 配置数组
     */
    Result<List<ConfigurationVo>> selectConfiguration();

    /**
     * 编辑某个配置的某个模式信息
     *
     * @param editEnvInfo 对象
     * @return 结果字符串
     */
    Result<String> editEnvConfig(EditEnvInfo editEnvInfo);

    /**
     * 获取所有存活的服务列表
     *
     * @return 字符串数组
     */
    Result<List<String>> getServiceList();
}
