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

package com.huawei.flowcontrol.adapte.cse.datasource;

import com.huawei.flowcontrol.common.adapte.cse.rule.AbstractRule;
import com.huawei.sermant.core.common.LoggerFactory;

import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.slots.block.Rule;

import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * CSE-kie规则数据
 *
 * @param <R> 流控规则
 * @param <S> Cse规则
 * @author zhouss
 * @since 2021-11-24
 */
public class CseKieDataSource<S extends AbstractRule, R extends Rule> extends AbstractDataSource<List<S>, List<R>> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private List<S> configData;

    public CseKieDataSource(Converter<List<S>, List<R>> parser) {
        super(parser);
    }

    /**
     * 更新配置
     *
     * @param config 配置
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void updateConfig(List<S> config) {
        configData = config;
        try {
            getProperty().updateValue(loadConfig());
        } catch (Exception ex) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Loaded config failed, %s", ex.getMessage()));
        }
    }

    @Override
    public List<S> readSource() {
        return configData;
    }

    @Override
    public void close() {
    }
}
