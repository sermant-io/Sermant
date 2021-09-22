package com.lubanops.apm.integration.transport.websocket;

import java.net.URI;

import org.java_websocket.handshake.ServerHandshake;

/**
 * @author
 * @since 2020/5/14
 **/
public class UnSecureWebSocketClient extends LubanWebSocketClient {
    public UnSecureWebSocketClient(URI serverUri, String uri) {
        super(serverUri, uri);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {

    }
}
