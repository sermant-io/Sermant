/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.backend.controller;

import com.huaweicloud.sermant.backend.cache.CollectorCache;
import com.huaweicloud.sermant.backend.entity.visibility.ServerInfo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service visibility API
 *
 * @author zhp
 * @since 2022-12-10
 */
@RestController
@RequestMapping("visibility")
public class VisibilityController {
    /**
     * Query service information
     *
     * @return service information
     */
    @GetMapping("/getCollectorInfo")
    public List<ServerInfo> getCollectorInfo() {
        if (CollectorCache.SERVER_MAP.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        return CollectorCache.SERVER_MAP.values().stream().collect(Collectors.toList());
    }
}
