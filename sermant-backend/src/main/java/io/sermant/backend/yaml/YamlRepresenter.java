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

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/**
 * Yaml serialized processor
 *
 * @author zhp
 * @since 2024-09-03
 */
public class YamlRepresenter extends Representer {
    /**
     * Constructor
     *
     * @param options Configuration for serialisation
     */
    public YamlRepresenter(DumperOptions options) {
        super(options);
    }

    @Override
    protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue,
                                                  Tag customTag) {
        if (property != null && propertyValue != null) {
            YamlField annotation = property.getAnnotation(YamlField.class);
            if (annotation != null) {
                String yamlKey = annotation.value();
                Node nodeValue = representData(propertyValue);
                Node nodeKey = representData(yamlKey);
                return new NodeTuple(nodeKey, nodeValue);
            }
        }
        return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
    }
}
