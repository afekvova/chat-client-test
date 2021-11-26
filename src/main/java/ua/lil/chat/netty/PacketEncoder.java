package ua.lil.chat.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import ua.lil.chat.protocol.AbstractPacket;

public class PacketEncoder extends MessageToByteEncoder<AbstractPacket> {

    protected void encode(ChannelHandlerContext ctx, AbstractPacket packet, ByteBuf buf) throws Exception {
        AbstractPacket.writePacket(packet, buf);
    }
}

