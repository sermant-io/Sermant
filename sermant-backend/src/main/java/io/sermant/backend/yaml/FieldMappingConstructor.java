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

import org.apache.commons.lang.StringUtils;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.FieldProperty;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeId;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * Yaml serialized processor
 *
 * @author zhp
 * @since 2024-09-03
 */
public class FieldMappingConstructor extends Constructor {
    /**
     * Create an instance
     *
     * @param loaderOptions - the configuration options
     */
    public FieldMappingConstructor(LoaderOptions loaderOptions) {
        super(loaderOptions);
        yamlClassConstructors.put(NodeId.mapping, new ConstructMapping());
    }

    /**
     * Construct Mapping
     *
     * @author zhp
     * @since 2024-09-03
     */
    private class ConstructMapping extends Constructor.ConstructMapping {
        @Override
        protected Property getProperty(Class<?> type, String name) {
            Optional<FieldProperty> fieldPropertyOptional = getFieldProperty(type, name);
            if (fieldPropertyOptional.isPresent()) {
                return fieldPropertyOptional.get();
            }
            for (Class<?> superClass = type.getSuperclass(); superClass != null; superClass =
                    superClass.getSuperclass()) {
                fieldPropertyOptional = getFieldProperty(superClass, name);
                if (fieldPropertyOptional.isPresent()) {
                    return fieldPropertyOptional.get();
                }
            }
            return super.getProperty(type, name);
        }

        /**
         * Retrieve the corresponding field based on the field name and class
         *
         * @param type class
         * @param name fileName
         * @return Property which is accessed as a field, without going through accessor methods
         */
        private Optional<FieldProperty> getFieldProperty(Class<?> type, String name) {
            for (Field field : type.getDeclaredFields()) {
                if (field.isAnnotationPresent(YamlField.class)
                        && StringUtils.equals(field.getAnnotation(YamlField.class).value(), name)) {
                    return Optional.of(new FieldProperty(field));
                }
            }
            return Optional.empty();
        }
    }
}
