/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.loadbalancer;

import com.huaweicloud.loadbalancer.service.RuleConverter;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.service.PluginService;
import com.huaweicloud.sermant.core.utils.StringUtils;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * yaml转换器
 *
 * @author zhouss
 * @since 2022-08-09
 */
public class YamlRuleConverter implements RuleConverter, PluginService {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final Yaml yaml;

    /**
     * 构造器
     */
    public YamlRuleConverter() {
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        yaml = new Yaml(representer);
    }

    @Override
    public <T> Optional<T> convert(String rawContent, Class<T> clazz) {
        if (StringUtils.isBlank(rawContent)) {
            return Optional.empty();
        }
        try {
            return Optional.of(yaml.loadAs(rawContent, clazz));
        } catch (YAMLException ex) {
            LOGGER.log(Level.WARNING, String.format(Locale.ENGLISH, "Can not convert content [%s] to LoadbalancerRule",
                    rawContent), ex);
        }
        return Optional.empty();
    }
}
