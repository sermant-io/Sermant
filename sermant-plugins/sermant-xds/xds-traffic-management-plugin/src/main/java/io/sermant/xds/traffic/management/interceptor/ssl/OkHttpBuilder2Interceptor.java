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

import com.squareup.okhttp.OkHttpClient;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.core.service.ServiceManager;
import io.sermant.core.service.xds.XdsCoreService;
import io.sermant.core.service.xds.XdsSecurityService;
import io.sermant.xds.traffic.management.cert.CertStoreCache;

import java.security.SecureRandom;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * OkHttpBuilder2 interception only for OkHttp2
 *
 * @author lilai
 * @since 2025-06-06
 */
public class OkHttpBuilder2Interceptor extends AbstractInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private XdsSecurityService xdsSecurityService = ServiceManager.getService(XdsCoreService.class)
            .getXdsSecurityService();

    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context)
            throws Exception {

        if (!xdsSecurityService.isSslEnabled()) {
            return context;
        }
        Object okhttpClientObject = context.getObject();
        if (!(okhttpClientObject instanceof OkHttpClient)) {
            return context;
        }

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        String password = xdsSecurityService.getIstiodCertificate().getPassword();
        keyManagerFactory.init(CertStoreCache.INSTANCE.getSermantKeyStore(),
                password == null ? null : password.toCharArray());
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(CertStoreCache.INSTANCE.getSermantTrustStore());

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(
                keyManagerFactory.getKeyManagers(),
                trustManagerFactory.getTrustManagers(),
                new SecureRandom()
        );

        OkHttpClient okHttpClient = (OkHttpClient) okhttpClientObject;
        okHttpClient.setSslSocketFactory(sslContext.getSocketFactory());
        return context;
    }
}
