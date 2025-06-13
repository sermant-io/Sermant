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

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import io.sermant.core.exception.SermantRuntimeException;
import io.sermant.core.service.xds.entity.IstiodCertificate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;

@RunWith(MockitoJUnitRunner.class)
public class SslConfigSourceTest {
    private static final String TEST_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC7VJTUt9Us8cKj\n" +
            "MzEfYyjiWA4R4/M2bS1GB4t7NXp98C3SC6dVMvDuictZoW2MPnZ1RszX5B+5ycky\n" +
            "-----END PRIVATE KEY-----";

    private static final String TEST_CERT_CHAIN = "-----BEGIN CERTIFICATE-----\n" +
            "MIIDWDCCAkCgAwIBAgIBATANBgkqhkiG9w0BAQsFADBOMQswCQYDVQQGEwJVUzET\n" +
            "-----END CERTIFICATE-----\n" +
            "-----BEGIN CERTIFICATE-----\n" +
            "MIIDWDCCAkCgAwIBAgIBATANBgkqhkiG9w0BAQsFADBOMQswCQYDVQQGEwJVUzET\n" +
            "-----END CERTIFICATE-----\n" +
            "-----BEGIN CERTIFICATE-----\n" +
            "MIIDWDCCAkCgAwIBAgIBATANBgkqhkiG9w0BAQsFADBOMQswCQYDVQQGEwJVUzET\n" +
            "-----END CERTIFICATE-----";

    @Mock
    private IstiodCertificate mockIstiodCertificate;

    @Mock
    private KeyStore mockKeyStore;

    @Mock
    private KeyStore mockTrustStore;

    @Mock
    private Certificate mockCertificate;

    @Mock
    private PrivateKey mockPrivateKey;

    @Mock
    private OutputStream mockOutputStream;

    @Before
    public void setUp() {
        when(mockIstiodCertificate.getPrivateKey()).thenReturn(TEST_PRIVATE_KEY);
        when(mockIstiodCertificate.getCertificateChain()).thenReturn(TEST_CERT_CHAIN);
        when(mockIstiodCertificate.getCertificateChain()).thenReturn(TEST_CERT_CHAIN);
    }

    @Test
    public void testConstructorWithValidCertificate() throws Exception {
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class);
                MockedStatic<Paths> mockedPaths = mockStatic(Paths.class);
                MockedStatic<KeyStore> mockedKeyStore = mockStatic(KeyStore.class);
                MockedStatic<CertificateFactory> mockedCertFactory = mockStatic(CertificateFactory.class);
                MockedStatic<KeyFactory> mockedKeyFactory = mockStatic(KeyFactory.class)) {

            setupMocks(mockedFiles, mockedPaths, mockedKeyStore, mockedCertFactory, mockedKeyFactory);

            SslConfigSource source = new SslConfigSource("test", mockIstiodCertificate);
            assertNotNull(source);
        }
    }

    @Test(expected = SermantRuntimeException.class)
    public void testConstructorWithException() {
        when(mockIstiodCertificate.getPrivateKey()).thenThrow(new RuntimeException("Test exception"));
        new SslConfigSource("test", mockIstiodCertificate);
    }

    @Test
    public void testProcessWithNullPassword() throws Exception {
        when(mockIstiodCertificate.getPassword()).thenReturn(null);

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class);
                MockedStatic<Paths> mockedPaths = mockStatic(Paths.class);
                MockedStatic<KeyStore> mockedKeyStore = mockStatic(KeyStore.class);
                MockedStatic<CertificateFactory> mockedCertFactory = mockStatic(CertificateFactory.class);
                MockedStatic<KeyFactory> mockedKeyFactory = mockStatic(KeyFactory.class)) {

            setupMocks(mockedFiles, mockedPaths, mockedKeyStore, mockedCertFactory, mockedKeyFactory);

            SslConfigSource source = new SslConfigSource("test", mockIstiodCertificate);
            assertNotNull(source);
        }
    }

    private void setupMocks(MockedStatic<Files> mockedFiles, MockedStatic<Paths> mockedPaths,
            MockedStatic<KeyStore> mockedKeyStore, MockedStatic<CertificateFactory> mockedCertFactory,
            MockedStatic<KeyFactory> mockedKeyFactory) throws Exception {
        // Mock Paths and Files
        Path mockPath = mock(Path.class);
        Path mockParent = mock(Path.class);
        when(mockPath.getParent()).thenReturn(mockParent);
        mockedPaths.when(() -> Paths.get(anyString())).thenReturn(mockPath);
        mockedFiles.when(() -> Files.exists(any(Path.class))).thenReturn(false);
        mockedFiles.when(() -> Files.createDirectories(any(Path.class))).thenReturn(mockPath);
        mockedFiles.when(() -> Files.createFile(any(Path.class))).thenReturn(mockPath);
        mockedFiles.when(() -> Files.newOutputStream(any(Path.class))).thenReturn(mockOutputStream);

        // Mock KeyStore
        mockedKeyStore.when(() -> KeyStore.getInstance(anyString())).thenReturn(mockKeyStore, mockTrustStore);
        doNothing().when(mockKeyStore).load(any(), any());
        doNothing().when(mockKeyStore).setKeyEntry(anyString(), any(), any(), any());
        doNothing().when(mockKeyStore).store(any(), any());
        doNothing().when(mockTrustStore).setCertificateEntry(anyString(), any());

        // Mock CertificateFactory
        CertificateFactory mockCertFactory = mock(CertificateFactory.class);
        mockedCertFactory.when(() -> CertificateFactory.getInstance(anyString())).thenReturn(mockCertFactory);
        when(mockCertFactory.generateCertificate(any(ByteArrayInputStream.class))).thenReturn(mockCertificate);

        // Mock KeyFactory
        KeyFactory mockKeyFactory = mock(KeyFactory.class);
        mockedKeyFactory.when(() -> KeyFactory.getInstance(anyString())).thenReturn(mockKeyFactory);
        when(mockKeyFactory.generatePrivate(any(PKCS8EncodedKeySpec.class))).thenReturn(mockPrivateKey);
    }
}
