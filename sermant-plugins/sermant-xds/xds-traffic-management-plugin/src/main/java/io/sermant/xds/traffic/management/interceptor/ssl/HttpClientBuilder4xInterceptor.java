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

package io.sermant.xds.traffic.management.interceptor.ssl;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.core.service.ServiceManager;
import io.sermant.core.service.xds.XdsCoreService;
import io.sermant.core.service.xds.XdsSecurityService;
import io.sermant.xds.traffic.management.cert.CertStoreCache;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;

import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

/**
 * HttpClientBuilder interception only for version 4. x
 *
 * @author lilai
 * @since 2025-06-06
 */
public class HttpClientBuilder4xInterceptor extends AbstractInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private XdsSecurityService xdsSecurityService = ServiceManager.getService(XdsCoreService.class)
            .getXdsSecurityService();

    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        if (!xdsSecurityService.isSslEnabled()) {
            return context;
        }
        Object httpClientBuilderObject = context.getObject();
        if (!(httpClientBuilderObject instanceof HttpClientBuilder)) {
            return context;
        }

        String password = xdsSecurityService.getIstiodCertificate().getPassword();
        HttpClientBuilder httpClientBuilder = (HttpClientBuilder) httpClientBuilderObject;
        SSLContext sslContext = SSLContexts.custom()
                .loadKeyMaterial(CertStoreCache.INSTANCE.getSermantKeyStore(),
                        password == null ? null : password.toCharArray())
                .loadTrustMaterial(CertStoreCache.INSTANCE.getSermantTrustStore(), null)
                .build();
        httpClientBuilder.setSSLContext(sslContext);
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}
