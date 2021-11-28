package ua.lil.chat.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ua.lil.chat.Connector;
import ua.lil.chat.helpers.LogHelper;
import ua.lil.chat.protocol.AbstractPacket;

public class PacketHandler extends SimpleChannelInboundHandler<AbstractPacket> {

    private final Connector connector = Connector.getInstance();

    public void channelInactive(ChannelHandlerContext ctx) {
        this.connector.onDisconnect();
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LogHelper.info("PacketHandler ERROR: " + cause.getMessage());
        ctx.close();
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, AbstractPacket packet) {
        if (Connector.isDebug())
            LogHelper.info("PacketHandler has read: " + packet.toString());

        this.connector.getListenerManager().handleListeners(packet);
    }
}

