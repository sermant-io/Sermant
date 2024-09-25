/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sermant.backend.controller;

import io.sermant.backend.common.conf.CommonConst;
import io.sermant.backend.entity.config.ConfigInfo;
import io.sermant.backend.entity.config.ConfigServerInfo;
import io.sermant.backend.entity.config.Result;
import io.sermant.backend.entity.config.ResultCodeType;
import io.sermant.backend.service.ConfigService;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import javax.annotation.Resource;

/**
 * Config Controller
 *
 * @author zhp
 * @since 2024-05-16
 */
@Component
@RestController
@RequestMapping("/sermant")
public class ConfigController {
    @Resource
    private ConfigService configService;

    /**
     * Query Configuration List
     *
     * @param configInfo Query criteria for configuration list
     * @return Configuration List
     */
    @GetMapping("/configs")
    public Result<List<ConfigInfo>> getConfigList(ConfigInfo configInfo) {
        if (StringUtils.isEmpty(configInfo.getPluginType())) {
            return new Result<>(ResultCodeType.MISS_PARAM.getCode(), ResultCodeType.MISS_PARAM.getMessage());
        }
        if (StringUtils.equals(configInfo.getPluginType(), CommonConst.COMMON_TEMPLATE)
                && StringUtils.isEmpty(configInfo.getGroupRule())) {
            return new Result<>(ResultCodeType.MISS_PARAM.getCode(), ResultCodeType.MISS_PARAM.getMessage());
        }
        return configService.getConfigList(configInfo);
    }

    /**
     * Query Configuration information
     *
     * @param configInfo Query criteria for configuration list
     * @return Configuration information
     */
    @GetMapping("/config")
    public Result<ConfigInfo> getConfig(ConfigInfo configInfo) {
        if (StringUtils.isEmpty(configInfo.getGroup()) || StringUtils.isEmpty(configInfo.getKey())) {
            return new Result<>(ResultCodeType.MISS_PARAM.getCode(), ResultCodeType.MISS_PARAM.getMessage());
        }
        return configService.getConfig(configInfo);
    }

    /**
     * Add configuration
     *
     * @param configInfo Configuration information
     * @return The result of adding configuration
     */
    @PostMapping("/config")
    public Result<Boolean> addConfig(@RequestBody ConfigInfo configInfo) {
        if (StringUtils.isEmpty(configInfo.getGroup()) || StringUtils.isEmpty(configInfo.getKey())
                || StringUtils.isEmpty(configInfo.getContent())) {
            return new Result<>(ResultCodeType.MISS_PARAM.getCode(), ResultCodeType.MISS_PARAM.getMessage());
        }
        Result<List<ConfigInfo>> result = configService.getConfigList(new ConfigInfo(configInfo.getKey(),
                configInfo.getGroup(), CommonConst.COMMON_TEMPLATE, true, configInfo.getNamespace()));
        if (CollectionUtils.isEmpty(result.getData())) {
            return configService.publishConfig(configInfo);
        }
        return new Result<>(ResultCodeType.EXISTS.getCode(), ResultCodeType.EXISTS.getMessage());
    }

    /**
     * Update configuration information
     *
     * @param configInfo Configuration information
     * @return The result of updating configuration information
     */
    @PutMapping("/config")
    public Result<Boolean> updateConfig(@RequestBody ConfigInfo configInfo) {
        if (StringUtils.isEmpty(configInfo.getGroup()) || StringUtils.isEmpty(configInfo.getKey())
                || StringUtils.isEmpty(configInfo.getContent())) {
            return new Result<>(ResultCodeType.MISS_PARAM.getCode(), ResultCodeType.MISS_PARAM.getMessage());
        }
        Result<List<ConfigInfo>> result = configService.getConfigList(new ConfigInfo(configInfo.getKey(),
                configInfo.getGroup(), CommonConst.COMMON_TEMPLATE, true, configInfo.getNamespace()));
        if (result.isSuccess() && CollectionUtils.isEmpty(result.getData())) {
            return new Result<>(ResultCodeType.NOT_EXISTS.getCode(), ResultCodeType.NOT_EXISTS.getMessage());
        }
        return configService.publishConfig(configInfo);
    }

    /**
     * delete configuration information
     *
     * @param configInfo Configuration information
     * @return The result of deleting configuration information
     */
    @DeleteMapping("/config")
    public Result<Boolean> deleteConfig(ConfigInfo configInfo) {
        if (StringUtils.isEmpty(configInfo.getGroup()) || StringUtils.isEmpty(configInfo.getKey())) {
            return new Result<>(ResultCodeType.MISS_PARAM.getCode(), ResultCodeType.MISS_PARAM.getMessage());
        }
        Result<List<ConfigInfo>> result = configService.getConfigList(new ConfigInfo(configInfo.getKey(),
                configInfo.getGroup(), CommonConst.COMMON_TEMPLATE, true, configInfo.getNamespace()));
        if (result.isSuccess() && CollectionUtils.isEmpty(result.getData())) {
            return new Result<>(ResultCodeType.NOT_EXISTS.getCode(), ResultCodeType.NOT_EXISTS.getMessage());
        }
        return configService.deleteConfig(configInfo);
    }

    /**
     * get configuration center information
     *
     * @return configuration center information
     */
    @GetMapping("/ConfigurationCenter")
    public Result<ConfigServerInfo> getConfigurationCenter() {
        return configService.getConfigurationCenter();
    }
}
