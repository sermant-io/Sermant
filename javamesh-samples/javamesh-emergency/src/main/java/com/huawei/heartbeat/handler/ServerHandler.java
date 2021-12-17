package com.huawei.heartbeat.handler;

import com.huawei.emergency.service.EmergencyAgentService;
import com.huawei.heartbeat.entity.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class ServerHandler extends SimpleChannelInboundHandler<Message.HeartbeatMessage> {
    public ServerHandler(EmergencyAgentService service){
        this.service = service;
    }

    private EmergencyAgentService service;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message.HeartbeatMessage msg) throws Exception {
        int type = msg.getMessageTypeValue();
        switch (type) {
            case Message.HeartbeatMessage.MessageType.HEARTBEAT_PING_VALUE:
                log.info("Heartbeat data received from the client");
                sendPongMsg(ctx,msg);
                break;
            case Message.HeartbeatMessage.MessageType.REGISTER_VALUE:
                InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
                String address = inetSocketAddress.getAddress().getHostAddress();
                String port = msg.getRegister().toStringUtf8();
                service.addAgent(address,port);
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
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("Close channelHandlerContext");
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Exception occurs. Exception info: {}", cause);
        ctx.close();
    }
}
