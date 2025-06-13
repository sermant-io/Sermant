/*
 * Copyright (C) 2025-2025 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.xds.traffic.management.inject;

import io.sermant.core.service.ServiceManager;
import io.sermant.core.service.xds.XdsCoreService;
import io.sermant.core.service.xds.entity.IstiodCertificate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * SSL configuration source
 *
 * @author lilai
 * @since 2025-06-03
 */
public class SpringEnvironmentProcessor implements EnvironmentPostProcessor {
    private static final String SOURCE_NAME = "SERMANT_XDS_SSL";

    private static final String DYNAMIC_PROPERTY_NAME = "Sermant-Dynamic-Config";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        final CompositePropertySource compositePropertySource = new CompositePropertySource(SOURCE_NAME);
        XdsCoreService xdsCoreService = ServiceManager.getService(XdsCoreService.class);
        IstiodCertificate istiodCertificate = xdsCoreService.getXdsSecurityService().getIstiodCertificate();
        compositePropertySource.addPropertySource(new SslConfigSource(SOURCE_NAME, istiodCertificate));
        if (environment.getPropertySources().contains(DYNAMIC_PROPERTY_NAME)) {
            environment.getPropertySources().addAfter(DYNAMIC_PROPERTY_NAME, compositePropertySource);
        } else {
            environment.getPropertySources().addFirst(compositePropertySource);
        }
    }
}
