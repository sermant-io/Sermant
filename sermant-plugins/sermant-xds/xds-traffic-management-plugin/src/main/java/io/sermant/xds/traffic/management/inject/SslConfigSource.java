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

import io.sermant.core.exception.SermantRuntimeException;
import io.sermant.core.service.xds.entity.IstiodCertificate;
import io.sermant.xds.traffic.management.cert.CertStoreCache;

import org.springframework.core.env.MapPropertySource;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SSL configuration source
 *
 * @author lilai
 * @since 2025-06-03
 */
public class SslConfigSource extends MapPropertySource {
    private static final String SERVER_SSL_KEY_STORE = "server.ssl.key-store";

    private static final String SERVER_SSL_KEY_PASSWORD = "server.ssl.key-password";

    private static final String SERVER_SSL_KEY_STORE_TYPE = "server.ssl.key-store-type";

    private static final String SERVER_SSL_KEY_ALIAS = "server.ssl.key-alias";

    private static final String SERVER_SSL_KEY_STORE_PASSWORD = "server.ssl.key-store-password";

    private static final String SERVER_SSL_TRUST_STORE = "server.ssl.trust-store";

    private static final String SERVER_SSL_TRUST_STORE_TYPE = "server.ssl.trust-store-type";

    private static final String SERVER_SSL_TRUST_STORE_PAASWORD = "server.ssl.trust-store-password";

    private static final Map<String, Object> SOURCE = new HashMap<>();

    private static final String PKCS_12 = "PKCS12";

    private static final String SERVER_KEY = "server-key";

    private static final String KEYSTORE_PATH = "/sermant/xds/keystore.p12";

    private static final String TRUSTSTORE_PATH = "/sermant/xds/truststore.p12";

    private static final String KEYSTOREPASS = "keystorepass";

    private static final String TRUSTSTOREPASS = "truststorepass";

    private static final String ROOT_CA = "root_ca";

    private static final String INTERMEDIATE_CA = "intermediate_ca";

    private static final String PEM_PATTERN = "-----BEGIN CERTIFICATE-----[\\s\\S]*?-----END CERTIFICATE-----";

    private static final int CERTCHAIN_LENGTH = 3;

    private static final int CA_INDEX = 2;

    /**
     * Constructor
     *
     * @param name source name
     * @param istiodCertificate Certificate from istiod
     * @throws SermantRuntimeException Build ssl config source error
     */
    public SslConfigSource(String name, IstiodCertificate istiodCertificate) {
        super(name, SOURCE);
        try {
            process(istiodCertificate);
        } catch (Exception e) {
            throw new SermantRuntimeException("Build ssl config source error", e);
        }
    }

    private void process(IstiodCertificate istiodCertificate)
            throws Exception {
        String privateKeyPem = istiodCertificate.getPrivateKey();
        String certificatePem = istiodCertificate.getCertificateChain();
        String password = istiodCertificate.getPassword();
        List<Certificate> certificateChain = new ArrayList<>();
        extractCertificateChain(certificatePem, certificateChain);
        PrivateKey privateKey = parsePrivateKey(privateKeyPem);
        KeyStore keyStore = KeyStore.getInstance(PKCS_12);
        keyStore.load(null, null);
        keyStore.setKeyEntry(
                SERVER_KEY,
                privateKey,
                password == null ? null : password.toCharArray(),
                certificateChain.toArray(new Certificate[0])
        );
        CertStoreCache.INSTANCE.updateKeyStore(keyStore);

        Path keyStorePath = Paths.get(KEYSTORE_PATH);
        Files.createDirectories(keyStorePath.getParent());
        if (!Files.exists(keyStorePath)) {
            Files.createFile(keyStorePath);
        }

        try (OutputStream os = Files.newOutputStream(keyStorePath)) {
            keyStore.store(os, KEYSTOREPASS.toCharArray());
        }

        KeyStore trustStore = KeyStore.getInstance(PKCS_12);
        trustStore.load(null, null);
        trustStore.setCertificateEntry(ROOT_CA, certificateChain.get(CA_INDEX));
        trustStore.setCertificateEntry(INTERMEDIATE_CA, certificateChain.get(1));
        Path truststorePath = Paths.get(TRUSTSTORE_PATH);
        Files.createDirectories(truststorePath.getParent());
        if (!Files.exists(truststorePath)) {
            Files.createFile(truststorePath);
        }
        try (OutputStream os = Files.newOutputStream(truststorePath)) {
            trustStore.store(os, TRUSTSTOREPASS.toCharArray());
        }
        CertStoreCache.INSTANCE.updateTrustStore(trustStore);

        SOURCE.put(SERVER_SSL_KEY_STORE, keyStorePath.toString());
        SOURCE.put(SERVER_SSL_KEY_PASSWORD, password);
        SOURCE.put(SERVER_SSL_KEY_STORE_TYPE, PKCS_12);
        SOURCE.put(SERVER_SSL_KEY_STORE_PASSWORD, KEYSTOREPASS);
        SOURCE.put(SERVER_SSL_KEY_ALIAS, SERVER_KEY);
        SOURCE.put(SERVER_SSL_TRUST_STORE, truststorePath.toString());
        SOURCE.put(SERVER_SSL_TRUST_STORE_TYPE, PKCS_12);
        SOURCE.put(SERVER_SSL_TRUST_STORE_PAASWORD, TRUSTSTOREPASS);
    }

    private void extractCertificateChain(String certificatePem, List<Certificate> certificateChain) throws Exception {
        Pattern pattern = Pattern.compile(PEM_PATTERN);
        Matcher matcher = pattern.matcher(certificatePem);

        while (matcher.find()) {
            certificateChain.add(parseCertificate(matcher.group()));
        }

        if (certificateChain.size() > CERTCHAIN_LENGTH) {
            Certificate lastCert = certificateChain.get(certificateChain.size() - 1);
            if (isSelfSigned(lastCert)) {
                certificateChain.remove(certificateChain.size() - 1);
            }
        }
    }

    private PrivateKey parsePrivateKey(String pem) throws Exception {
        String base64 = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] der = Base64.getDecoder().decode(base64);

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private Certificate parseCertificate(String pem) throws Exception {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return factory.generateCertificate(
                new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8))
        );
    }

    private boolean isSelfSigned(Certificate cert) {
        if (!(cert instanceof X509Certificate)) {
            return false;
        }
        X509Certificate x509 = (X509Certificate) cert;
        return x509.getIssuerX500Principal().equals(x509.getSubjectX500Principal());
    }
}
