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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.service.ServiceManager;
import io.sermant.core.service.xds.XdsCoreService;
import io.sermant.core.service.xds.XdsSecurityService;
import io.sermant.core.service.xds.entity.IstiodCertificate;
import io.sermant.xds.traffic.management.cert.CertStoreCache;

import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;

/**
 * Test for {@link HttpClientBuilder4xInterceptor}
 */
public class HttpClientBuilder4xInterceptorTest {
    private HttpClientBuilder4xInterceptor interceptor;

    private MockedStatic<ServiceManager> serviceManagerMockedStatic;

    private ExecuteContext executeContext;

    private HttpClientBuilder httpClientBuilder;

    private XdsCoreService xdsCoreService;

    private XdsSecurityService xdsSecurityService;

    @BeforeEach
    void setUp() throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        xdsCoreService = mock(XdsCoreService.class);
        xdsSecurityService = mock(XdsSecurityService.class);
        serviceManagerMockedStatic = Mockito.mockStatic(ServiceManager.class);
        serviceManagerMockedStatic.when(() -> ServiceManager.getService(XdsCoreService.class))
                .thenReturn(xdsCoreService);
        when(xdsCoreService.getXdsSecurityService()).thenReturn(xdsSecurityService);
        executeContext = mock(ExecuteContext.class);
        httpClientBuilder = mock(HttpClientBuilder.class);
        interceptor = new HttpClientBuilder4xInterceptor();
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        CertStoreCache.INSTANCE.updateTrustStore(keyStore);
        CertStoreCache.INSTANCE.updateKeyStore(keyStore);
    }

    @AfterEach
    void tearDown() {
        serviceManagerMockedStatic.close();
        CertStoreCache.INSTANCE.updateTrustStore(null);
        CertStoreCache.INSTANCE.updateKeyStore(null);
    }

    /**
     * TC1: SSL is disabled
     */
    @Test
    void testBeforeWhenSslDisabled() throws Exception {
        ExecuteContext result = interceptor.before(executeContext);
        assertSame(executeContext, result);
    }

    /**
     * TC2: SSL enabled but context object is not HttpClientBuilder
     */
    @Test
    void testBeforeWhenObjectNotHttpClientBuilder() throws Exception {
        when(executeContext.getObject()).thenReturn(new Object()); // Not HttpClientBuilder

        ExecuteContext result = interceptor.before(executeContext);
        assertSame(executeContext, result);
    }

    /**
     * TC3: SSL enabled with valid HttpClientBuilder and empty password
     */
    @Test
    void testBeforeWhenSslEnabledAndPasswordEmpty() throws Exception {
        when(xdsSecurityService.isSslEnabled()).thenReturn(true);
        when(executeContext.getObject()).thenReturn(httpClientBuilder);
        when(xdsSecurityService.getIstiodCertificate()).thenReturn(
                new IstiodCertificate("privateKey", "certChain", 100, 100));

        // Assume CertStoreCache is initialized (may need real setup in actual environment)
        interceptor.before(executeContext);
        // Verify SSLContext is set with null password
        verify(httpClientBuilder).setSSLContext(any(SSLContext.class));
    }

    /**
     * TC4: SSL enabled with valid HttpClientBuilder and non-empty password
     */
    @Test
    void testBeforeWhenSslEnabledAndPasswordNonEmpty() throws Exception {
        when(xdsSecurityService.isSslEnabled()).thenReturn(true);
        when(executeContext.getObject()).thenReturn(httpClientBuilder);
        when(xdsSecurityService.getIstiodCertificate()).thenReturn(
                new IstiodCertificate("privateKey", "certChain", "123456", 100, 100));

        interceptor.before(executeContext);

        // Verify SSLContext is set with password
        verify(httpClientBuilder).setSSLContext(any(SSLContext.class));
    }
}
