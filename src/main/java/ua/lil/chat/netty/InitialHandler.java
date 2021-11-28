package ua.lil.chat.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ua.lil.chat.Connector;
import ua.lil.chat.helpers.LogHelper;
import ua.lil.chat.protocol.AbstractPacket;
import ua.lil.chat.protocol.HandshakePacket;

import java.net.InetSocketAddress;

public class InitialHandler extends SimpleChannelInboundHandler<AbstractPacket> {

    private final Connector connector = Connector.getInstance();

    public void channelActive(ChannelHandlerContext ctx) {
        LogHelper.info("InitialHandler has connected! Sending handshake packet...");
        HandshakePacket packet = new HandshakePacket();
        packet.setName(connector.getName());
        ctx.writeAndFlush(packet);
    }

    public void channelInactive(ChannelHandlerContext ctx) {
        LogHelper.info("InitialHandler has disconneted!");
        this.connector.reconnect();
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LogHelper.info("[/" + InitialHandler.getChannelIp(ctx.channel()) + "] InitialHandler ERROR: " + cause.getMessage());
        ctx.close();
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, AbstractPacket packet) {
        if (packet instanceof HandshakePacket) {

            HandshakePacket handshakePacket = (HandshakePacket) packet;
            if (!handshakePacket.isAllowed()) {
                LogHelper.info("Channel close! Disconnecting...");
                LogHelper.error(handshakePacket.getCancelReason());
                channelHandlerContext.close();
                return;
            }

            LogHelper.info("InitialHandler has read Handshake! Changing pipeline...");
            this.connector.setName(((HandshakePacket) packet).getName());
            channelHandlerContext.pipeline().removeLast();
            channelHandlerContext.pipeline().addLast(new PacketHandler());
        } else {
            LogHelper.info("First packet must be Handshake! Disconnecting...");
            channelHandlerContext.close();
        }
    }

    public static String getChannelIp(Channel channel) {
        return ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
    }
}

