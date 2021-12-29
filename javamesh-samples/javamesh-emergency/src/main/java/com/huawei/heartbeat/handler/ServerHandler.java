package com.huawei.heartbeat.handler;

import com.google.protobuf.ByteString;
import com.huawei.emergency.service.EmergencyAgentService;
import com.huawei.heartbeat.entity.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ServerHandler extends SimpleChannelInboundHandler<Message.HeartbeatMessage> {
    private EmergencyAgentService service;

    private String serverPort;

    public ServerHandler(EmergencyAgentService service, String serverPort) {
        this.service = service;
        this.serverPort = serverPort;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message.HeartbeatMessage msg) {
        int type = msg.getMessageTypeValue();
        switch (type) {
            case Message.HeartbeatMessage.MessageType.HEARTBEAT_PING_VALUE:
                log.info("Heartbeat data received from the client");
                sendPongMsg(ctx, msg);
                break;
            case Message.HeartbeatMessage.MessageType.REGISTER_VALUE:
                String address = getAddress(ctx);
                String port = msg.getRegister().toStringUtf8();
                service.addAgent(address, port);
                Message.HeartbeatMessage message = Message.HeartbeatMessage.newBuilder()
                        .setMessageType(Message.HeartbeatMessage.MessageType.REGISTER)
                        .setRegister(ByteString.copyFrom(serverPort.getBytes(StandardCharsets.UTF_8)))
                        .build();
                ctx.channel().writeAndFlush(message);
                break;
            default:
                break;
        }
    }

    private void sendPongMsg(ChannelHandlerContext ctx, Message.HeartbeatMessage msg) {
        Message.HeartbeatMessage message = msg.newBuilderForType()
                .setMessageType(Message.HeartbeatMessage.MessageType.HEARTBEAT_PONG)
                .setHeartBeat(Message.HeartBeat.newBuilder().build())
                .build();
        Channel channel = ctx.channel();
        channel.writeAndFlush(message);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        IdleStateEvent stateEvent = (IdleStateEvent) evt;
        switch (stateEvent.state()) {
            case READER_IDLE:
                handlerReaderIdle(ctx);
                break;
            default:
                break;
        }
    }

    private void handlerReaderIdle(ChannelHandlerContext ctx) {
        log.info("Client timeOut, close it");
        service.removeAgent(getAddress(ctx));
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("Close channelHandlerContext");
        service.removeAgent(getAddress(ctx));
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Exception occurs. Exception info: {}", cause);
        service.removeAgent(getAddress(ctx));
        ctx.close();
    }

    private String getAddress(ChannelHandlerContext ctx) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        return inetSocketAddress.getAddress().getHostAddress();
    }
}
