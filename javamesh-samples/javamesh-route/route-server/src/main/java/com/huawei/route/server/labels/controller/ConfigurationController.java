/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.controller;

import com.huawei.route.common.Result;
import com.huawei.route.server.labels.configuration.Configuration;
import com.huawei.route.server.labels.configuration.ConfigurationVo;
import com.huawei.route.server.labels.configuration.EditEnvInfo;
import com.huawei.route.server.labels.configuration.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 配置管理控制类
 *
 * @author Zhang Hu
 * @since 2021-04-15
 */
@RestController
public class ConfigurationController {
    private final ConfigurationService configurationService;

    @Autowired
    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    /**
     * 添加配置
     *
     * @param configuration 配置信息
     * @return Result
     */
    @PostMapping("/configuration/add")
    public Result<String> addConfig(@Validated @RequestBody Configuration configuration) {
        return configurationService.addConfiguration(configuration);
    }

    /**
     * 更新配置
     *
     * @param configuration 配置信息
     * @return Result
     */
    @PostMapping("/configuration/update")
    public Result<String> updateConfig(@Validated @RequestBody Configuration configuration) {
        return configurationService.updateConfiguration(configuration);
    }

    /**
     * 根据配置名删除配置
     *
     * @param configName 配置名称
     * @return Result
     */
    @PostMapping("/configuration/delete")
    public Result<String> deleteConfig(String configName) {
        return configurationService.deleteConfiguration(configName);
    }

    /**
     * 查询配置
     *
     * @return Result
     */
    @GetMapping("/configurations")
    public Result<List<ConfigurationVo>> selectConfig() {
        return configurationService.selectConfiguration();
    }

    /**
     * 查询配置的服务
     *
     * @return Result
     */
    @GetMapping("/services")
    public Result<List<String>> selectServices() {
        return configurationService.getServiceList();
    }

    /**
     * 编辑环境变量
     *
     * @param envInfo 环境配置
     * @return Result
     */
    @PostMapping("/configuration/env")
    public Result<String> editEnvConfig(@Validated @RequestBody EditEnvInfo envInfo) {
        return configurationService.editEnvConfig(envInfo);
    }
}
