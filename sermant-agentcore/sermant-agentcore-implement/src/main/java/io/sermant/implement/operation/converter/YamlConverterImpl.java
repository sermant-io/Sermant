/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.implement.operation.converter;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.operation.converter.api.YamlConverter;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.ConstructorException;
import org.yaml.snakeyaml.representer.Representer;

import java.io.Reader;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Yaml converter implementation class
 *
 * @author luanwenfei
 * @since 2022-06-22
 */
public class YamlConverterImpl implements YamlConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final Yaml yaml;

    /**
     * Constructor.
     */
    public YamlConverterImpl() {
        Representer representer = new Representer(new DumperOptions());
        representer.getPropertyUtils().setSkipMissingProperties(true);
        yaml = new Yaml(representer);
    }

    @Override
    public <T> Optional<T> convert(String source, Class<? super T> type) {
        try {
            return Optional.ofNullable((T)yaml.loadAs(source, type));
        } catch (ConstructorException ex) {
            LOGGER.warning(
                String.format(Locale.ENGLISH, "There were some errors when converting from String, target type : "
                    + "[%s], source : [%s], error message : [%s]", type.getName(), source, ex.getMessage()));
        }
        return Optional.empty();
    }

    @Override
    public <T> Optional<T> convert(Reader reader, Class<? super T> type) {
        try {
            return Optional.ofNullable((T)yaml.loadAs(reader, type));
        } catch (ConstructorException ex) {
            LOGGER.warning(
                String.format(Locale.ENGLISH, "There were some errors when converting from Reader, target type : "
                    + "[%s], source : [%s], error message : [%s]", type.getName(), reader, ex.getMessage()));
        }
        return Optional.empty();
    }

    @Override
    public String dump(Object data) {
        return yaml.dump(data);
    }
}
