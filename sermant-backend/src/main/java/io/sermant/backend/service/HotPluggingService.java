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

package io.sermant.backend.service;

import io.sermant.backend.entity.config.Result;
import io.sermant.backend.entity.config.ResultCodeType;
import io.sermant.backend.entity.hotplugging.CommandType;
import io.sermant.backend.entity.hotplugging.HotPluggingConfig;
import io.sermant.implement.service.dynamicconfig.ConfigClient;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

/**
 * Hot plugging Service
 *
 * @author zhp
 * @since 2024-08-22
 */
@Service
public class HotPluggingService {
    private static final String HOT_PLUGGING_CONFIG_KEY = "config";

    private static final String HOT_PLUGGING_CONFIG_GROUP = "sermant-hot-plugging";

    @Resource
    private ConfigService configService;

    private final Yaml yaml = new Yaml();

    private final Set<String> commandTypeSet = EnumSet.allOf(CommandType.class).stream().map(CommandType::getValue)
            .collect(Collectors.toSet());

    /**
     * publish configuration
     *
     * @param hotPluggingConfig hot plugging configuration information
     * @return The result of publish hot plugging configuration
     */
    public Result<Boolean> publishHotPluggingConfig(HotPluggingConfig hotPluggingConfig) {
        if (!validateParam(hotPluggingConfig)) {
            return new Result<>(ResultCodeType.MISS_PARAM);
        }
        ConfigClient configClient = configService.getConfigClient();
        boolean result = configClient.publishConfig(HOT_PLUGGING_CONFIG_KEY, HOT_PLUGGING_CONFIG_GROUP,
                yaml.dumpAsMap(hotPluggingConfig));
        if (result) {
            return new Result<>(ResultCodeType.SUCCESS);
        }
        return new Result<>(ResultCodeType.FAIL);
    }

    /**
     * validate Param
     *
     * @param hotPluggingConfig hot plugging configuration information
     * @return verification results
     */
    private boolean validateParam(HotPluggingConfig hotPluggingConfig) {
        return StringUtils.isNotEmpty(hotPluggingConfig.getPluginNames())
                || !commandTypeSet.contains(hotPluggingConfig.getCommandType());
    }
}
