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

import io.sermant.backend.yaml.YamlField;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Element information of configuration management page template
 *
 * @author zhp
 * @since 2024-09-02
 */
@Getter
@Setter
public class Element {
    /**
     * english description
     */
    @YamlField("desc-en")
    private String englishDesc;

    /**
     * chinese description
     */
    @YamlField("desc-zh")
    private String chineseDesc;

    /**
     * name of element
     */
    private String name;

    /**
     * Selectable options for the element
     */
    private List<Option> values;

    /**
     * The placeholder in an input box is a short, descriptive text that appears within the input field when it is empty
     */
    private Description placeholder;

    /**
     * Required
     */
    private boolean required;

    /**
     * Prompt
     */
    private Description notice;
}
