package com.huawei.javamesh.core.lubanops.core.utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;

class MyConnectionSocketFactory extends PlainConnectionSocketFactory {

    @Override
    public Socket createSocket(final HttpContext httpContext) throws IOException {
        if (httpContext.getAttribute("proxy") != null) {
            String proxyAddr = (String) httpContext.getAttribute("proxy");
            InetSocketAddress socksaddr = new InetSocketAddress(proxyAddr, 38335);
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
            return new Socket(proxy);
        } else {
            return super.createSocket(httpContext);
        }
    }

}