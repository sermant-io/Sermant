/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.flowcontrol.common.core.resolver;

import com.huawei.flowcontrol.common.core.constants.CseConstants;
import com.huawei.flowcontrol.common.core.resolver.listener.ConfigUpdateListener;
import com.huawei.flowcontrol.common.core.rule.Configurable;
import com.huawei.flowcontrol.common.entity.FlowControlServiceMeta;
import com.huawei.flowcontrol.common.util.StringUtils;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.operation.OperationManager;
import com.huaweicloud.sermant.core.operation.converter.api.YamlConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * abstract analytic class
 *
 * @param <T> ruleEntity
 * @author zhouss
 * @since 2022-08-11
 */
public abstract class AbstractResolver<T extends Configurable> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * configure prefixes for all types of rules
     */
    private final String configKey;

    /**
     * regularData map serviceScenarioName, regularData
     */
    private final Map<String, T> rules;

    /**
     * Configure the update listener to be parsed and then notified
     */
    private final List<ConfigUpdateListener<T>> listeners = new ArrayList<>();

    /**
     * the parser constructor
     *
     * @param configKey parser configuration key
     */
    public AbstractResolver(String configKey) {
        this.configKey = configKey;
        rules = new HashMap<>();
    }

    /**
     * register listener
     *
     * @param listener listener
     */
    public synchronized void registerListener(ConfigUpdateListener<T> listener) {
        listeners.add(listener);
    }

    /**
     * configuration update notification
     *
     * @param updateKey updated the service scenario name key
     */
    public void notifyListeners(String updateKey) {
        for (ConfigUpdateListener<T> listener : listeners) {
            try {
                listener.notify(updateKey, rules);
            } catch (Exception ex) {
                LOGGER.warning(String.format(Locale.ENGLISH, "Notified listener failed when updating rule! %s",
                        ex.getMessage()));
            }
        }
    }

    /**
     * formatting rule
     *
     * @param businessKey service Scenario name
     * @param value business rule
     * @param isOverride Whether to override the rule when updating a single business scenario
     * @param isForDelete To delete the service, delete the service configuration directly
     * @return converted rules
     */
    public Optional<T> parseRule(String businessKey, String value, boolean isOverride, boolean isForDelete) {
        if (StringUtils.isEmpty(businessKey)) {
            return Optional.empty();
        }
        if (isForDelete) {
            rules.remove(businessKey);
            return Optional.empty();
        }

        // The value is null, and the user deletes the service scenario name
        if (StringUtils.isEmpty(value) && isOverride) {
            rules.remove(businessKey);
            return Optional.empty();
        }

        // 1、remove the old configuration
        rules.remove(businessKey);

        // 2、convert this configuration
        final Optional<T> optionalRule = OperationManager.getOperation(YamlConverter.class)
                .convert(value,getRuleClass());
        if (!optionalRule.isPresent()) {
            return optionalRule;
        }
        final T rule = optionalRule.get();

        // 3、set the name and service name
        rule.setName(businessKey);

        // 4、determine whether the rule is legal
        if (rule.isInValid()) {
            return Optional.empty();
        }
        if (!isServicesMatch(rule.getServices())) {
            return Optional.empty();
        }
        if (isOverride) {
            rules.put(businessKey, rule);
        }
        return Optional.of(rule);
    }

    /**
     * gets the rule entity type
     *
     * @return type
     */
    protected abstract Class<T> getRuleClass();

    /**
     * gets the parser configuration prefix
     *
     * @param configKey parser configuration key
     * @return configuration prefix
     */
    public static String getConfigKeyPrefix(String configKey) {
        return configKey + ".";
    }

    /**
     * Matching Service name and Version The version must be obtained by blocking the sdk
     *
     * @param services Services. Multiple services are separated by commas (,)
     * @return match the version
     */
    private boolean isServicesMatch(String services) {
        if (StringUtils.isEmpty(services)) {
            return true;
        }
        for (String service : services.split(CseConstants.SERVICE_SEPARATOR)) {
            String[] serviceAndVersion = service.split(CseConstants.SERVICE_VERSION_SEPARATOR);

            // service name matching
            if (serviceAndVersion.length == 1 && serviceAndVersion[0]
                    .equals(FlowControlServiceMeta.getInstance().getServiceName())) {
                return true;
            }

            // service plus version matching
            if (serviceAndVersion.length == CseConstants.SERVICE_VERSION_PARTS && serviceAndVersion[0]
                    .equals(FlowControlServiceMeta.getInstance().getServiceName())
                    && serviceAndVersion[1].equals(FlowControlServiceMeta.getInstance().getVersion())) {
                return true;
            }
        }
        return false;
    }

    public String getConfigKey() {
        return configKey;
    }

    public Map<String, T> getRules() {
        return rules;
    }
}
