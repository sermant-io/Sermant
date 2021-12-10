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

package com.huawei.javamesh.core.lubanops.integration.transport.websocket;

import java.net.Socket;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.huawei.javamesh.core.lubanops.integration.exception.JavaagentRuntimeException;

/**
 * 安全的LubanWebSockent的client
 * @author
 * @since 2020/5/14
 **/
public class SecureWebSocketClient extends LubanWebSocketClient {

    TrustManager[] trustAllCerts = new TrustManager[] {
        new X509ExtendedTrustManager() {

            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
                    throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
                    throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1, Socket arg2)
                    throws CertificateException {

            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2)
                    throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1, Socket arg2)
                    throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2)
                    throws CertificateException {

            }
        }
    };

    public SecureWebSocketClient(URI serverUri, String uri) {

        super(serverUri, uri);
        SSLContext ssLContext;
        try {
            ssLContext = SSLContext.getInstance("TLS");
            ssLContext.init(null, trustAllCerts, new java.security.SecureRandom());

        } catch (NoSuchAlgorithmException e) {
            throw new JavaagentRuntimeException(e);
        } catch (KeyManagementException e) {
            throw new JavaagentRuntimeException(e);
        }

        SSLSocketFactory sslSocketfactory = ssLContext.getSocketFactory();
        ((WebSocketClient) this).setSocketFactory(sslSocketfactory);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {

    }

}
