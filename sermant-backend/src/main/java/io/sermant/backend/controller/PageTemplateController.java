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
import io.sermant.backend.entity.template.PageTemplateInfo;
import io.sermant.backend.service.PageTemplateService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import javax.annotation.Resource;

/**
 * Template controller for configuration management page
 *
 * @author zhp
 * @since 2024-08-22
 */
@RestController
@RequestMapping("/sermant")
public class PageTemplateController {
    @Resource
    private PageTemplateService pageTemplateService;

    /**
     * Get the template information for configuration management page
     *
     * @return the template information
     */
    @GetMapping("/templates")
    public Result<List<PageTemplateInfo>> getTemplateInfo() {
        return pageTemplateService.getTemplateList();
    }
}
