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

package io.sermant.backend.entity.template;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * template information for configuration management page
 *
 * @author zhp
 * @since 2024-09-02
 */
@Getter
@Setter
public class PageTemplateInfo {
    /**
     * plugin names
     */
    private Name plugin;

    /**
     * Generation rules for configuration item groups
     */
    private List<String> groupRule;

    /**
     * Generation rules for configuration item keys
     */
    private List<String> keyRule;

    /**
     * Element Collection
     */
    private List<Element> elements;

    /**
     * Template information for plugin configuration
     */
    private List<ConfigTemplate> configTemplates;
}
