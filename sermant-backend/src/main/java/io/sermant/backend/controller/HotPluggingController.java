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

import io.sermant.backend.entity.config.Result;
import io.sermant.backend.entity.config.ResultCodeType;
import io.sermant.backend.entity.hotplugging.HotPluggingConfig;
import io.sermant.backend.service.HotPluggingService;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Hot Plugging controller
 *
 * @author zhp
 * @since 2024-08-22
 */
@RestController
@RequestMapping("/sermant")
public class HotPluggingController {
    @Resource
    private HotPluggingService hotPluggingService;

    /**
     * publish configuration
     *
     * @param hotPluggingConfig hot plugging configuration information
     * @return The result of publish hot plugging configuration
     */
    @PostMapping("/publishHotPluggingConfig")
    public Result<Boolean> publishHotPluggingConfig(@RequestBody HotPluggingConfig hotPluggingConfig) {
        if (hotPluggingConfig == null || StringUtils.isEmpty(hotPluggingConfig.getCommandType())
                || StringUtils.isEmpty(hotPluggingConfig.getInstanceIds())) {
            return new Result<>(ResultCodeType.MISS_PARAM);
        }
        return hotPluggingService.publishHotPluggingConfig(hotPluggingConfig);
    }
}
