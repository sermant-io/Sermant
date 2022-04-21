/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.sermant.core.plugin.converter;

import com.huawei.sermant.core.common.LoggerFactory;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * yaml转换器
 *
 * @param <T> 目标类型
 * @author zhouss
 * @since 2022-04-14
 */
public class YamlConverter<T> implements Converter<String, T> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    protected final Yaml yaml;

    /**
     * 目标类类型
     */
    private final Class<?> targetClass;

    /**
     * yaml转换器构造
     *
     * @param targetClass 目标类型
     */
    public YamlConverter(Class<?> targetClass) {
        this.targetClass = targetClass;
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        yaml = new Yaml(representer);
    }

    @Override
    public Optional<T> convert(String source) {
        if (targetClass == null) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable((T) yaml.loadAs(source, targetClass));
        } catch (Exception ex) {
            LOGGER.warning(String.format(Locale.ENGLISH,
                "There were some errors when converting, target type : "
                    + "[%s], source : [%s], error message : [%s]",
                targetClass.getName(), source, ex.getMessage()));
        }
        return Optional.empty();
    }
}
