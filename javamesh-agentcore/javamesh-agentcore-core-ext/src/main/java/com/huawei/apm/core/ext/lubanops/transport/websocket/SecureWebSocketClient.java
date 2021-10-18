package com.huawei.apm.core.ext.lubanops.transport.websocket;

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

import com.huawei.apm.core.ext.lubanops.exception.JavaagentRuntimeException;

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
