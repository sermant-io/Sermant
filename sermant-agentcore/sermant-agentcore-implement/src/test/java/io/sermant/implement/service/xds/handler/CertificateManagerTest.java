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

package io.sermant.implement.service.xds.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import io.sermant.core.config.ConfigManager;
import io.sermant.core.service.xds.config.XdsConfig;
import io.sermant.core.service.xds.entity.IstiodCertificate;
import io.sermant.core.utils.FileUtils;
import io.sermant.implement.service.xds.cache.XdsDataCache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.security.KeyPair;

@RunWith(MockitoJUnitRunner.class)
public class CertificateManagerTest {
    private static final String MOCK_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6Im03bDVram83c2JTRi1QcDk4d0JkUUNBSG4wMUdaZmlpWDhsdFV0b1JHMGMifQ.eyJhdWQiOlsiaXN0aW8tY2EiXSwiZXhwIjoxNzUxNzg0MzIwLCJpYXQiOjE3NDkxOTIzMjAsImlzcyI6Imh0dHBzOi8va3ViZXJuZXRlcy5kZWZhdWx0LnN2Yy5jbHVzdGVyLmxvY2FsIiwianRpIjoiYjY1NzU3OWYtMTMxZC00YjE2LThjZTUtMWI2Mjg5NDhmOGI1Iiwia3ViZXJuZXRlcy5pbyI6eyJuYW1lc3BhY2UiOiJkZWZhdWx0Iiwic2VydmljZWFjY291bnQiOnsibmFtZSI6InNlcm1hbnQiLCJ1aWQiOiIyN2I2MThkMC0zMWU2LTQ5ODEtODNkOC00MjRiYTUwOGZkMDkifX0sIm5iZiI6MTc0OTE5MjMyMCwic3ViIjoic3lzdGVtOnNlcnZpY2VhY2NvdW50OmRlZmF1bHQ6c2VybWFudCJ9.eGOaSfuBTIKCjx8nS5IwaQIXKcFFR-FfIDk0RjV66i6oJusuRyJomrFJPUGfnp195hdz_1cU1DIF3a-cayQtOtSnUyS_UEQXvP-xGLb38VdphiqOlJRMetqB7Ntz24rL56ftDygk5jhbAXVcLn3GMSmLDAC-MCCYH7kdOYB2j5JGhmtA-1JwUKZRrA2LNkaflgSHgG3bvvKouIsRveil8J0aRsAxjUcnRPXSyIuXU-U3xy-wGLRflOrsazGwXZ0AzlPCQo9vcpXULxcRB6wKt8OncpQL66GYZyqXdjvh2R71KRHrggEAhAsMI913OY1GtykzMkw1mJlPtsEAamlz6g";

    private static final String MOCK_CERT_CHAIN = "-----BEGIN CERTIFICATE-----\nMOCK_CERT\n-----END CERTIFICATE-----";

    private static final String MOCK_PRIVATE_KEY = "-----BEGIN RSA PRIVATE KEY-----\nMOCK_KEY\n-----END RSA PRIVATE KEY-----";

    private XdsConfig xdsConfig;

    @Mock
    private KeyPair mockKeyPair;

    private CertificateManager certificateManager;

    private MockedStatic<ConfigManager> configManagerMock;

    private MockedStatic<FileUtils> fileUtilsMock;

    private MockedStatic<XdsDataCache> dataCacheMock;

    @Before
    public void setUp() throws Exception {
        // Mock static classes
        xdsConfig = new XdsConfig();
        configManagerMock = Mockito.mockStatic(ConfigManager.class);
        configManagerMock.when(() -> ConfigManager.getConfig(XdsConfig.class)).thenReturn(xdsConfig);

        fileUtilsMock = Mockito.mockStatic(FileUtils.class);
        fileUtilsMock.when(() -> FileUtils.readFileToString(anyString())).thenReturn(MOCK_TOKEN);

        dataCacheMock = Mockito.mockStatic(XdsDataCache.class);
        certificateManager = new CertificateManager();
    }

    @After
    public void tearDown() {
        configManagerMock.close();
        fileUtilsMock.close();
        dataCacheMock.close();
    }

    @Test
    public void testGenerateCertificatePairSecurityDisabled() {
        xdsConfig.setSecurityEnable(false);
        certificateManager.generateCertificatePair();

        // Verify no interactions with data cache
        dataCacheMock.verifyNoInteractions();
    }

    @Test
    public void testGenerateCertificatePairSuccess() throws Exception {
        xdsConfig.setSecurityEnable(true);
        xdsConfig.setTokenPath("/path/istio/token");
        // Prepare test data
        CertificateManager spyManager = spy(certificateManager);

        doReturn(MOCK_CERT_CHAIN).when(spyManager).requestCertificateFromIstio(anyString(), anyString());

        spyManager.generateCertificatePair();

        // Verify certificate was stored
        ArgumentCaptor<IstiodCertificate> certCaptor = ArgumentCaptor.forClass(IstiodCertificate.class);
        dataCacheMock.verify(() -> XdsDataCache.setIstiodCertificate(certCaptor.capture()));

        IstiodCertificate cert = certCaptor.getValue();
        assertNotNull(cert);
        assertEquals(MOCK_CERT_CHAIN, cert.getCertificateChain());
        assertTrue(cert.getCreateTime() <= System.currentTimeMillis());
        assertTrue(cert.getExpireTime() > System.currentTimeMillis());
    }
}
