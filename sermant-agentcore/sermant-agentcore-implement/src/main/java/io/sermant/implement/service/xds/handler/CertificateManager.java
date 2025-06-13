/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on dubbo-xds/src/main/java/org/apache/dubbo/xds/security/istio/IstioCitadelCertificateSigner.java
 * from the Apache dubbo project.
 */

package io.sermant.implement.service.xds.handler;

import com.alibaba.fastjson.JSONObject;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.config.ConfigManager;
import io.sermant.core.exception.SermantRuntimeException;
import io.sermant.core.service.xds.config.XdsConfig;
import io.sermant.core.service.xds.entity.IstiodCertificate;
import io.sermant.core.utils.FileUtils;
import io.sermant.implement.service.xds.cache.XdsDataCache;
import istio.v1.auth.Certificate.IstioCertificateRequest;
import istio.v1.auth.Certificate.IstioCertificateResponse;
import istio.v1.auth.IstioCertificateServiceGrpc;

import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.ExtensionsGenerator;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObject;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLException;

/**
 * Certificate Manager - Responsible for communicating with the Istio Certificate Service and generating certificate
 * pairs
 *
 * @author lilai
 * @since 2025-05-26
 */
public class CertificateManager {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final int DEFAULT_KEY_SIZE = 2048;

    private static final int DEFAULT_CERT_TTL_SECONDS = 86400;

    private static final String DEFAULT_TRUST_DOMAIN = "cluster.local";

    private static final String RSA_ALGORITHM = "RSA";

    private static final String SIGNATURE_ALGORITHM = "SHA256WithRSA";

    private static final String PRIVATE_KEY_PEM_TYPE = "RSA PRIVATE KEY";

    private static final String CSR_PEM_TYPE = "CERTIFICATE REQUEST";

    private static final String AUTH_HEADER_KEY = "authorization";

    private static final String BEARER_PREFIX = "Bearer ";

    private static final String NS = "/ns/";

    private static final String SPIFFE = "spiffe://";

    private static final String SA = "/sa/";

    private static final int INVALD_LENGTH = 2;

    private final XdsConfig config = ConfigManager.getConfig(XdsConfig.class);

    /**
     * Create certificate
     *
     * @throws SermantRuntimeException Certificate generation failed
     */
    public void generateCertificatePair() {
        if (!config.isSecurityEnable()) {
            return;
        }
        try {
            String istioToken = FileUtils.readFileToString(config.getTokenPath());
            cacheServiceAccountAndNamespace(istioToken);
            KeyPair keyPair = generateRsaKeyPair();
            String csr = generateCertificateSigningRequest(keyPair);
            String certificateChain = requestCertificateFromIstio(csr, istioToken);
            String privateKeyPem;
            JcaPKCS8Generator gen = new JcaPKCS8Generator(keyPair.getPrivate(), null);
            StringWriter writer = new StringWriter();
            try (JcaPEMWriter pemWriter = new JcaPEMWriter(writer)) {
                pemWriter.writeObject(gen.generate());
            }
            privateKeyPem = writer.toString();

            long expirationTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(DEFAULT_CERT_TTL_SECONDS);
            XdsDataCache.setIstiodCertificate(new IstiodCertificate(
                    privateKeyPem,
                    certificateChain,
                    System.currentTimeMillis(),
                    expirationTime
            ));
        } catch (Exception e) {
            throw new SermantRuntimeException("Certificate generation failed", e);
        }
    }

    /**
     * Generate RSA key pair
     */
    private KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        keyGen.initialize(DEFAULT_KEY_SIZE);
        return keyGen.generateKeyPair();
    }

    /**
     * Generate Certificate Signing Request (CSR)
     */
    private String generateCertificateSigningRequest(KeyPair keyPair)
            throws OperatorCreationException, IOException {

        ContentSigner signer = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM)
                .build(keyPair.getPrivate());

        GeneralName[] subjectAltNames = {new GeneralName(GeneralName.dNSName,
                SPIFFE + DEFAULT_TRUST_DOMAIN + NS + XdsDataCache.getCurrentNameSpace() + SA
                        + XdsDataCache.getCurrentServiceAccount())};
        GeneralNames san = new GeneralNames(subjectAltNames);

        ExtensionsGenerator extGenerator = new ExtensionsGenerator();
        extGenerator.addExtension(Extension.subjectAlternativeName, true, san);

        PKCS10CertificationRequest csr = new JcaPKCS10CertificationRequestBuilder(
                new X500Name("O=" + DEFAULT_TRUST_DOMAIN),
                keyPair.getPublic())
                .addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest, extGenerator.generate())
                .build(signer);

        LOGGER.fine("Generated CSR for Istio CA");
        return convertToPem(CSR_PEM_TYPE, csr.getEncoded());
    }

    /**
     * Request certificate from Istio
     *
     * @param csr Certificate Signing Request
     * @param token istio token
     * @return Certificate
     * @throws IOException IOException
     * @throws SermantRuntimeException Generate Cert Failed
     */
    public String requestCertificateFromIstio(String csr, String token)
            throws IOException {
        ManagedChannel channel = createSecureChannel();
        IstioCertificateServiceGrpc.IstioCertificateServiceStub stub =
                createAuthenticatedStub(channel, token);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        StringBuilder certChainBuilder = new StringBuilder();
        StreamObserver<IstioCertificateResponse> responseObserver =
                createResponseHandler(countDownLatch, certChainBuilder);

        stub.createCertificate(buildCertificateRequest(csr), responseObserver);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new SermantRuntimeException("Generate Cert Failed.");
        }

        return certChainBuilder.toString();
    }

    /**
     * Create gRPC channel
     */
    private ManagedChannel createSecureChannel() throws SSLException {
        return NettyChannelBuilder.forTarget(config.getControlPlaneAddress())
                .sslContext(GrpcSslContexts.forClient()
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build())
                .build();
    }

    /**
     * Create gRPC stub with authentication
     */
    private IstioCertificateServiceGrpc.IstioCertificateServiceStub createAuthenticatedStub(
            ManagedChannel channel, String token) {
        Metadata headers = new Metadata();
        Key<String> authKey = Key.of(AUTH_HEADER_KEY, Metadata.ASCII_STRING_MARSHALLER);
        String jwtToken = BEARER_PREFIX + token;
        headers.put(authKey, jwtToken);
        return MetadataUtils.attachHeaders(
                IstioCertificateServiceGrpc.newStub(channel),
                headers
        );
    }

    /**
     * Build certificate request
     */
    private IstioCertificateRequest buildCertificateRequest(String csr) {
        return IstioCertificateRequest.newBuilder()
                .setCsr(csr)
                .setValidityDuration(DEFAULT_CERT_TTL_SECONDS)
                .build();
    }

    /**
     * Create response processor
     */
    private StreamObserver<IstioCertificateResponse> createResponseHandler(
            CountDownLatch countDownLatch, StringBuilder certChainBuilder) {

        return new StreamObserver<IstioCertificateResponse>() {
            @Override
            public void onNext(IstioCertificateResponse response) {
                for (int i = 0; i < response.getCertChainCount(); i++) {
                    certChainBuilder.append(response.getCertChainBytes(i).toStringUtf8());
                }
                LOGGER.log(Level.FINE, "Cert chain from istiod is :{0}", certChainBuilder);
                countDown(countDownLatch);
            }

            @Override
            public void onError(Throwable throwable) {
                countDown(countDownLatch);
                LOGGER.log(Level.SEVERE, "Certificate request failed", throwable);
            }

            @Override
            public void onCompleted() {
                countDown(countDownLatch);
                LOGGER.log(Level.WARNING,
                        "Xds stream for Certificate is closed, new stream has been created for communication.");
            }
        };
    }

    /**
     * Convert data to PEM format
     */
    private String convertToPem(String type, byte[] content) throws IOException {
        try (StringWriter writer = new StringWriter();
                JcaPEMWriter pemWriter = new JcaPEMWriter(writer)) {

            pemWriter.writeObject(new PemObject(type, content));
            pemWriter.flush();
            return writer.toString();
        }
    }

    private void countDown(CountDownLatch countDownLatch) {
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }

    private void cacheServiceAccountAndNamespace(String token) {
        String payload = extractJwtPayload(token);
        JSONObject root = JSONObject.parseObject(payload);
        JSONObject k8s = root.getJSONObject("kubernetes.io");
        String namespace = k8s.getString("namespace");
        XdsDataCache.setCurrentNameSpace(namespace);
        JSONObject serviceAccount = k8s.getJSONObject("serviceaccount");
        String serviceAccountName = serviceAccount.getString("name");
        XdsDataCache.setCurrentServiceAccount(serviceAccountName);
    }

    /**
     * Extract JWT token from payload
     */
    private String extractJwtPayload(String token) {
        // JWT format: header.payload.signature
        String[] parts = token.split("\\.");
        if (parts.length < INVALD_LENGTH) {
            throw new IllegalArgumentException("Invalid JWT token format");
        }
        return new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
    }
}
