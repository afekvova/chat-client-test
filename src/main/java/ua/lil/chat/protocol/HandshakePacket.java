package ua.lil.chat.protocol;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HandshakePacket extends AbstractPacket {

    String name;
    boolean allowed;

    public HandshakePacket() {
        super((short) 0);
    }

    @Override
    protected void read(ByteBuf buf) {
        this.name = HandshakePacket.readString(buf);
        this.allowed = buf.readBoolean();
    }

    @Override
    protected void write(ByteBuf buf) {
        HandshakePacket.writeString(buf, this.name);
        buf.writeBoolean(this.allowed);
    }
}

