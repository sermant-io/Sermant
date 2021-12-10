/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol.adapte.cse.converter;

import com.huawei.javamesh.core.common.LoggerFactory;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * yaml格式字符串转换器
 *
 * @author zhouss
 * @since 2021-11-16
 */
public class YamlConverter<TARGET> implements Converter<String, TARGET> {
    private static final Logger LOGGER = LoggerFactory.getLogger();
    /**
     * 目标类类型
     */
    private final Class<TARGET> targetClass;

    private final Representer representer = new Representer();

    public YamlConverter(Class<TARGET> targetClass) {
        this.targetClass = targetClass;
        representer.getPropertyUtils().setSkipMissingProperties(true);
    }

    @Override
    public TARGET convert(String source) {
        if (targetClass == null) {
            return null;
        }
        try {
            Yaml yaml = new Yaml(new Constructor(new TypeDescription(targetClass, targetClass)), representer);
            return yaml.loadAs(source, targetClass);
        } catch (Exception ex) {
            LOGGER.warning(String.format(Locale.ENGLISH,
                    "There were some errors when convert rule, target rule : [%s], source : [%s], error message : [%s]"
            , targetClass.getName(), source, ex.getMessage()));
        }
        return null;
    }
}
