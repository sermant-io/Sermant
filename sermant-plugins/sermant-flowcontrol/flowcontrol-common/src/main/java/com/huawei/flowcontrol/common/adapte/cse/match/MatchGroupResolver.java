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

package com.huawei.flowcontrol.common.adapte.cse.match;

import com.huawei.flowcontrol.common.adapte.cse.converter.YamlConverter;
import com.huawei.flowcontrol.common.adapte.cse.resolver.AbstractResolver;
import com.huawei.sermant.core.common.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 业务组
 *
 * @author zhouss
 * @since 2021-11-16
 */
public class MatchGroupResolver extends AbstractResolver<BusinessMatcher> {
    /**
     * 业务场景匹配 键值
     */
    public static final String CONFIG_KEY = "servicecomb.matchGroup";

    public MatchGroupResolver() {
        super(CONFIG_KEY, new MatchGroupConverter(BusinessMatcher.class));
    }

    @Override
    protected Class<BusinessMatcher> getRuleClass() {
        return BusinessMatcher.class;
    }

    public static class MatchGroupConverter extends YamlConverter<BusinessMatcher> {
        private static final Logger LOGGER = LoggerFactory.getLogger();

        public MatchGroupConverter(Class<BusinessMatcher> businessMatcherClass) {
            super(businessMatcherClass);
        }

        /**
         * 由于yaml低版本复杂对象无法直接转换，因此进行手动赋值
         *
         * @param source 配置字符串
         * @return matcher
         */
        @Override
        @SuppressWarnings("checkstyle:IllegalCatch")
        public BusinessMatcher convert(String source) {
            final ClassLoader appClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                // 此处需使用PluginClassLoader, 需要拿到指定的转换类
                Thread.currentThread().setContextClassLoader(YamlConverter.class.getClassLoader());
                final Map<Object, Object> yamlSource = yaml.load(source);
                final BusinessMatcher businessMatcher = new BusinessMatcher();
                businessMatcher.setName((String) yamlSource.get("name"));
                final List<LinkedHashMap<Object, Object>> matches = (List<LinkedHashMap<Object, Object>>) yamlSource
                    .get("matches");
                final List<RequestMatcher> requestMatchers = new ArrayList<>();
                for (LinkedHashMap<Object, Object> map : matches) {
                    final RequestMatcher requestMatcher = new RequestMatcher();
                    final LinkedHashMap<String, String> pathMap = (LinkedHashMap<String, String>) map.get("apiPath");
                    requestMatcher.setApiPath(new RawOperator(pathMap));
                    requestMatcher.setMethod((List<String>) map.get("method"));
                    requestMatcher.setName((String) map.get("name"));
                    requestMatcher.setHeaders((Map<String, RawOperator>) map.get("headers"));
                    requestMatchers.add(requestMatcher);
                }
                businessMatcher.setMatches(requestMatchers);
                businessMatcher.setServices((String) yamlSource.get("service"));
                return businessMatcher;
            } catch (Exception ex) {
                LOGGER.warning(String.format(Locale.ENGLISH,
                    "There were some errors when convert rule, target rule : "
                        + "[%s], source : [%s], error message : [%s]",
                    BusinessMatcher.class.getName(), source, ex.getMessage()));
                return null;
            } finally {
                Thread.currentThread().setContextClassLoader(appClassLoader);
            }
        }
    }
}
