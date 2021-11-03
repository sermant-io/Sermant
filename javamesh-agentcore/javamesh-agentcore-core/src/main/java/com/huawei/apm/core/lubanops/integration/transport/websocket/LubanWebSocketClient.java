package com.huawei.apm.core.lubanops.integration.transport.websocket;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.java_websocket.client.WebSocketClient;

import com.alibaba.fastjson.JSON;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.lubanops.integration.access.Message;
import com.huawei.apm.core.lubanops.integration.access.MessageType;
import com.huawei.apm.core.lubanops.integration.access.outbound.SessionOpenRequest.SessionOpenHeader;
import com.huawei.apm.core.lubanops.integration.transport.websocket.future.FutureManagementService;
import com.huawei.apm.core.lubanops.integration.transport.websocket.future.MessageFuture;

/**
 * @author
 * @since 2020/4/21
 **/
public abstract class LubanWebSocketClient extends WebSocketClient {

    private final static Logger LOGGER = LogFactory.getLogger();

    private boolean hasOpenResult = false;

    private CountDownLatch latch = new CountDownLatch(1);

    private boolean openResult = false;

    // 注意要重连
    private final FutureManagementService futureManagementService = FutureManagementService.getInstance();

    private final String uri;

    public LubanWebSocketClient(URI serverUri, String uri) {
        super(serverUri);
        this.uri = uri;
    }

    /**
     * 异步发送
     * @param message
     */
    public void sendAsync(Message message) {
        super.send(message.toBytes());
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "sendAsync,message id:" + message.getMessageId());
        }
    }

    /**
     * 同步发送
     * @param message
     */
    public Message sendSync(Message message) {
        Message response = null;
        MessageFuture future = futureManagementService.getFuture(message.getMessageId());
        super.send(message.toBytes());
        response = future.get();
        if (LOGGER.isLoggable(Level.FINE)) {
            if (response == null) {
                LOGGER.log(Level.FINE, String.format("got nop response message,id:[%s]", message.getMessageId()));
            } else {
                LOGGER.log(Level.FINE, "got response message, id:" + message.getMessageId());
            }
        }
        return response;
    }

    @Override
    public void onMessage(String s) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "onMessage message:" + s);
        }
    }

    @Override
    public void onMessage(ByteBuffer buffer) {
        int length = buffer.limit() - buffer.position();
        byte[] bytes = new byte[length];
        buffer.get(bytes);

        Message msg = Message.parseBytes(bytes);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, " received message , id:" + msg.getMessageId());
        }
        if (msg.getMessageId() == 0 && LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, " received message , msg" + msg);
        }

        if (msg.getType() == MessageType.MONITOR_DATA_RESPONSE) {
            futureManagementService.notifyFuture(msg);

        } else if (msg.getType() == MessageType.TRACE_EVENT_RESPONSE) {
            futureManagementService.notifyFuture(msg);
        } else if (msg.getType() == MessageType.ACCESS_SESSION_OPEN_RESPONSE) {
            SessionOpenHeader sessionOpenHeader = JSON.parseObject(msg.getHeader(), SessionOpenHeader.class);
            if ("0".equals(sessionOpenHeader.getCode())) {
                openResult = true;
            } else {
                openResult = false;
                LOGGER.log(Level.SEVERE, "access_session_open_response id:" + msg.getMessageId() + " result:"
                        + sessionOpenHeader.getMsg());
            }
            hasOpenResult = true;
            latch.countDown();
        } else if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "can not enter here" + msg.getMessageId());

        }

    }

    @Override
    public void onClose(int i, String s, boolean b) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "onClose " + s + ", boolean:" + b);
        }
    }

    @Override
    public void onError(Exception e) {
        LOGGER.log(Level.SEVERE, "on error", e);
    }

    public String getUri() {
        return uri;
    }

    public boolean isHasOpenResult(long timeout) {
        try {
            latch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
        return hasOpenResult;
    }

    public boolean isHasOpenResult() {
        return hasOpenResult;
    }

    public void setHasOpenResult(boolean hasOpenResult) {
        this.hasOpenResult = hasOpenResult;
    }

    public boolean getOpenResult() {
        return openResult;
    }

    public void setOpenResult(boolean openResult) {
        this.openResult = openResult;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LubanWebSocketClient{");
        sb.append("uri=").append(uri);
        sb.append('}');
        return sb.toString();
    }
}
