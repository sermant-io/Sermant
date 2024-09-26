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

package io.sermant.backend.yaml;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * YAML annotation, used to specify the field names for mapping
 *
 * @author zhp
 * @since 2024-09-03
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface YamlField {
    /**
     * Specify an alias for the YAML field
     *
     * @return an alias for the YAML field
     */
    String value();
}
